package com.syos.integration;

import com.syos.adapter.in.cli.commands.POSCommand;
import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import com.syos.adapter.in.cli.session.UserSession;
import com.syos.adapter.out.persistence.memory.InMemoryItemMasterFileRepository;
import com.syos.adapter.out.persistence.memory.InMemoryShelfStockRepository;
import com.syos.application.services.DiscountService;
import com.syos.domain.entities.ItemMasterFile;
import com.syos.domain.entities.ShelfStock;
import com.syos.domain.valueobjects.*;
import com.syos.infrastructure.persistence.repositories.JpaPOSRepository;
import com.syos.shared.enums.UserRole;
import com.syos.domain.entities.User;
import com.syos.shared.enums.UnitOfMeasure;
import com.syos.domain.valueobjects.ActiveStatus;
import com.syos.domain.valueobjects.MemberSince;
import com.syos.domain.valueobjects.UpdatedAt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("POSCommand Integration-like Tests (in-memory repos + mocks)")
public class POSCommandIntegrationTest {

    private InMemoryItemMasterFileRepository itemRepo;
    private InMemoryShelfStockRepository shelfRepo;
    private ConsoleIO console;
    private DiscountService discountService;
    private JpaPOSRepository posRepo;

    @BeforeEach
    void setup() {
        itemRepo = new InMemoryItemMasterFileRepository();
        shelfRepo = new InMemoryShelfStockRepository();
        console = mock(ConsoleIO.class);
        discountService = mock(DiscountService.class);
        posRepo = mock(JpaPOSRepository.class);

        // Ensure a logged-in EMPLOYEE for POS
        SessionManager.getInstance().clearSession();
        User employee = User.createWithRole(
                Username.of("emp1"),
                "password123",
                Name.of("Emp One"),
                Email.of("e@e.com"),
                UserRole.EMPLOYEE,
                UserID.of(999L)
        );
        // Reconstitute with explicit ID so SessionManager returns a non-null userId
        employee = User.withId(
                UserID.of(1L),
                employee.getUsername(),
                employee.getPassword(),
                employee.getRole(),
                employee.getName(),
                employee.getEmail(),
                employee.getSynexPoints(),
                employee.getActiveStatus(),
                employee.getCreatedAt(),
                UpdatedAt.of(employee.getUpdatedAt()),
                employee.getCreatedBy(),
                MemberSince.of(employee.getCreatedAt())
        );
        SessionManager.getInstance().createSession(new UserSession(employee));
    }

    private ItemMasterFile seedItem(String code, String name, double price) {
        ItemMasterFile item = new ItemMasterFile.Builder()
                .itemCode(ItemCode.of(code))
                .itemName(name)
                .description("")
                .brandId(BrandId.of(1L))
                .categoryId(CategoryId.of(1L))
                .supplierId(SupplierId.of(1L))
                .unitOfMeasure(UnitOfMeasure.EACH)
                .packSize(PackSize.of(1))
                .costPrice(new Money(BigDecimal.valueOf(price)))
                .sellingPrice(new Money(BigDecimal.valueOf(price)))
                .reorderPoint(ReorderPoint.of(50))
                .createdBy(UserID.of(1L))
                .build();
        return itemRepo.save(item);
    }

    private void seedShelfBatch(ItemMasterFile item, long batchId, double qty, LocalDateTime placed, LocalDateTime expiry, double unitPrice) {
        ShelfStock ss = new ShelfStock.Builder()
                .itemCode(item.getItemCode())
                .itemId(item.getId())
                .batchId(batchId)
                .shelfCode("A1")
                .quantityOnShelf(Quantity.of(BigDecimal.valueOf(qty)))
                .placedOnShelfDate(placed)
                .expiryDate(expiry)
                .placedBy(UserID.of(1L))
                .unitPrice(new Money(BigDecimal.valueOf(unitPrice)))
                .lastUpdatedBy(UserID.of(1L))
                .build();
        shelfRepo.save(ss);
    }

    @Test
    @DisplayName("Happy path sale applies per-batch discounts and reduces shelf stock with expiry override")
    void posHappyPathWithDiscountsAndExpiryOverride() {
        // Seed one item and two shelf batches
        ItemMasterFile item = seedItem("ITM100", "Coca-Cola", 200.00);
        // Older placed but later expiry
        seedShelfBatch(item, 11L, 3.0, LocalDateTime.now().minusDays(10), LocalDateTime.now().plusDays(90), 200.00);
        // Newer placed but earlier expiry (should be prioritized)
        seedShelfBatch(item, 22L, 5.0, LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(10), 200.00);

        // Mock discounts per batch: batch 22 has 10 LKR per unit, batch 11 has none
        when(discountService.calculateBatchDiscount(eq(item.getId()), eq(22L), any(), anyDouble()))
                .thenAnswer(inv -> BigDecimal.valueOf(10).multiply(BigDecimal.valueOf(inv.getArgument(3, Double.class))).setScale(2));
        when(discountService.calculateBatchDiscount(eq(item.getId()), eq(11L), any(), anyDouble()))
                .thenReturn(BigDecimal.ZERO.setScale(2));

        // Mock persistence result with bill no 1
        when(posRepo.savePOSCheckout(any(), any())).thenReturn(new JpaPOSRepository.PersistResult(1L, "1"));

        // Console input: item code, qty, cash, enter to continue
        when(console.readLine(anyString()))
                .thenReturn("ITM100")   // item code
                .thenReturn("6")        // quantity (6 units)
                .thenReturn("")         // checkout
                .thenReturn("1500")     // cash tendered
                .thenReturn("");        // press enter to continue

        POSCommand pos = new POSCommand(console, SessionManager.getInstance(), shelfRepo, itemRepo, discountService, posRepo);
        pos.execute();

        // Capture persisted lines to ensure allocation order and discounts
        ArgumentCaptor<List<JpaPOSRepository.PosLine>> captor = ArgumentCaptor.forClass(List.class);
        verify(posRepo).savePOSCheckout(any(), captor.capture());
        List<JpaPOSRepository.PosLine> lines = captor.getValue();
        assertNotNull(lines);
        // Expected: 6 units total, first from batch 22 (expiring sooner) up to 5, then 1 from batch 11
        double totalQty = lines.stream().mapToDouble(JpaPOSRepository.PosLine::quantity).sum();
        assertEquals(6.0, totalQty, 0.0001);
        assertTrue(lines.stream().anyMatch(l -> l.batchId() == 22L && Math.abs(l.quantity() - 5.0) < 1e-9));
        assertTrue(lines.stream().anyMatch(l -> l.batchId() == 11L && Math.abs(l.quantity() - 1.0) < 1e-9));

        // Verify discount applied: 10 per unit for 5 units = 50.00 in total
        double disc = lines.stream().map(l -> l.discount() == null ? BigDecimal.ZERO : l.discount()).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
        assertEquals(50.00, disc, 0.01);

        // Verify shelf quantities reduced accordingly
        BigDecimal remainingQty = shelfRepo.getCurrentStock(item.getId());
        assertEquals(2.0, remainingQty.doubleValue(), 0.0001); // 8 initial - 6 sold = 2 remaining

        // Verify console shows bill, net, and change
        verify(console, atLeastOnce()).println(contains("===== BILL (POS) ====="));
        verify(console, atLeastOnce()).println(contains("Bill No: 1"));
        verify(console, atLeastOnce()).println(contains("Channel: POS"));
    }

    @Test
    @DisplayName("Personal purchase mode disables discounts and enforces 10,000 limit")
    void personalPurchaseRules() {
        ItemMasterFile item = seedItem("ITM200", "Pepsi", 1000.00);
        seedShelfBatch(item, 33L, 20.0, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30), 1000.00);

        // Discount service should not be called in personal mode, but mock safe default
        when(discountService.calculateBatchDiscount(anyLong(), anyLong(), any(), anyDouble())).thenReturn(BigDecimal.valueOf(9999));
        when(posRepo.savePOSCheckout(any(), any())).thenReturn(new JpaPOSRepository.PersistResult(2L, "2"));

        // Toggle personal mode with 'P', then buy 12, which is 12,000 > limit -> should cancel
        when(console.readLine(anyString()))
                .thenReturn("P")        // toggle personal mode
                .thenReturn("ITM200")   // item code
                .thenReturn("12")       // qty
                .thenReturn("")         // checkout
                .thenReturn("15000")    // cash, but shouldn't reach here due to limit
                .thenReturn("");

        POSCommand pos = new POSCommand(console, SessionManager.getInstance(), shelfRepo, itemRepo, discountService, posRepo);
        pos.execute();

        // Ensure error printed and no persistence invoked
        verify(console).printError(contains("exceeds limit"));
        verify(posRepo, never()).savePOSCheckout(any(), any());

        // Now perform a valid personal purchase under limit: buy 5 x 1000 = 5000
        reset(console);
        when(console.readLine(anyString()))
                .thenReturn("P")        // toggle personal mode
                .thenReturn("ITM200")
                .thenReturn("5")
                .thenReturn("")
                .thenReturn("6000")
                .thenReturn("");

        POSCommand pos2 = new POSCommand(console, SessionManager.getInstance(), shelfRepo, itemRepo, discountService, posRepo);
        pos2.execute();

        // Discounts should be zero in printed summary
        verify(console, atLeastOnce()).println(contains("DISCOUNT: -LKR 0.00"));
    }
}

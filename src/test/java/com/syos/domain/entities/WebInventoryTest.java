package com.syos.domain.entities;

import com.syos.domain.valueobjects.ItemCode;
import com.syos.domain.valueobjects.Money;
import com.syos.domain.valueobjects.Quantity;
import com.syos.domain.valueobjects.UserID;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WebInventoryTest {

    private ItemCode code() { return ItemCode.of("ITEM-003"); }
    private Quantity qty(String v) { return Quantity.of(new BigDecimal(v)); }
    private UserID user(long id) { return UserID.of(id); }

    @Test
    void create_sell_restock_publish_feature() {
        WebInventory web = WebInventory.createNew(code(), 3L, 300L, qty("20"), LocalDateTime.now().plusDays(5), user(1), Money.of("500.00"));
        assertTrue(web.isAvailableForPurchase());
        assertEquals(20, web.getStockLevel());

        WebInventory sold = web.sellStock(qty("5"), user(2));
        assertEquals(qty("15"), sold.getQuantityAvailable());
        assertTrue(sold.isLowStock());
        assertTrue(sold.isExpiringSoon());

        WebInventory restocked = sold.restockWeb(qty("10"), user(3));
        assertEquals(qty("25"), restocked.getQuantityAvailable());

        WebInventory priced = restocked.updateWebPrice(Money.of("550.00"), user(3));
        assertEquals(Money.of("550.00"), priced.getWebPrice());

        WebInventory published = priced.setPublishStatus(true, user(3));
        WebInventory featured = published.setFeaturedStatus(true, user(3));
        assertTrue(published.isPublished());
        assertTrue(featured.isFeatured());

        WebInventory content = featured.updateWebContent("Nice product", "grocery,beverage", user(3));
        assertEquals("Nice product", content.getWebDescription());
        assertEquals("grocery,beverage", content.getSeoKeywords());
    }

    @Test
    void invalidConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new WebInventory.Builder()
                .itemCode(code()).itemId(1L).batchId(1L)
                .quantityAvailable(Quantity.of(new BigDecimal("-1")))
                .addedToWebDate(LocalDateTime.now()).addedBy(user(1))
                .webPrice(Money.of("100")).lastUpdatedBy(user(1)).build());
    }
}

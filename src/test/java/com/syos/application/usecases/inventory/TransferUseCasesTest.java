package com.syos.application.usecases.inventory;

import com.syos.application.ports.out.ShelfStockRepository;
import com.syos.application.ports.out.StockTransferRepository;
import com.syos.application.ports.out.WarehouseStockRepository;
import com.syos.application.ports.out.WebInventoryRepository;
import com.syos.application.strategies.stock.StockSelectionStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferUseCasesTest {

    @Test
    void transferToWebUseCase_constructorNulls_andDeprecatedExecute() {
        // Constructor null validations
        WebInventoryRepository webRepo = null;
        WarehouseStockRepository warehouseRepo = org.mockito.Mockito.mock(WarehouseStockRepository.class);
        StockTransferRepository transferRepo = org.mockito.Mockito.mock(StockTransferRepository.class);
        StockSelectionStrategy strategy = org.mockito.Mockito.mock(StockSelectionStrategy.class);
        assertThrows(NullPointerException.class, () -> new TransferToWebUseCase(warehouseRepo, webRepo, transferRepo, strategy));
        assertThrows(NullPointerException.class, () -> new TransferToWebUseCase(null, org.mockito.Mockito.mock(WebInventoryRepository.class), transferRepo, strategy));
        assertThrows(NullPointerException.class, () -> new TransferToWebUseCase(warehouseRepo, org.mockito.Mockito.mock(WebInventoryRepository.class), null, strategy));
        assertThrows(NullPointerException.class, () -> new TransferToWebUseCase(warehouseRepo, org.mockito.Mockito.mock(WebInventoryRepository.class), transferRepo, null));

        // Deprecated execute path
        TransferToWebUseCase useCase = new TransferToWebUseCase(
                warehouseRepo,
                org.mockito.Mockito.mock(WebInventoryRepository.class),
                transferRepo,
                strategy
        );
        assertThrows(UnsupportedOperationException.class, () -> useCase.transfer(1L, new BigDecimal("5")));
    }

    @Test
    void transferToShelfUseCase_constructorNulls_andDeprecatedExecute() {
        WarehouseStockRepository warehouseRepo = org.mockito.Mockito.mock(WarehouseStockRepository.class);
        ShelfStockRepository shelfRepo = org.mockito.Mockito.mock(ShelfStockRepository.class);
        StockTransferRepository transferRepo = org.mockito.Mockito.mock(StockTransferRepository.class);
        StockSelectionStrategy strategy = org.mockito.Mockito.mock(StockSelectionStrategy.class);

        // Null validations
        assertThrows(NullPointerException.class, () -> new TransferToShelfUseCase(null, shelfRepo, transferRepo, strategy));
        assertThrows(NullPointerException.class, () -> new TransferToShelfUseCase(warehouseRepo, null, transferRepo, strategy));
        assertThrows(NullPointerException.class, () -> new TransferToShelfUseCase(warehouseRepo, shelfRepo, null, strategy));
        assertThrows(NullPointerException.class, () -> new TransferToShelfUseCase(warehouseRepo, shelfRepo, transferRepo, null));

        // Deprecated execute path
        TransferToShelfUseCase useCase = new TransferToShelfUseCase(warehouseRepo, shelfRepo, transferRepo, strategy);
        assertThrows(UnsupportedOperationException.class, () -> useCase.transfer(1L, new BigDecimal("10")));
    }
}

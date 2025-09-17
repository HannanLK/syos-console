package com.syos.application.ports.out;

import java.math.BigDecimal;

public interface ShelfStockRepository {
    void addToShelf(long itemId, long batchId, BigDecimal quantity);
    BigDecimal getCurrentStock(long itemId);
}

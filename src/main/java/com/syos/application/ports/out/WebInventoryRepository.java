package com.syos.application.ports.out;

import java.math.BigDecimal;

public interface WebInventoryRepository {
    void addToWebInventory(long itemId, long batchId, BigDecimal quantity);
    BigDecimal getCurrentStock(long itemId);
}

package com.syos.application.ports.out;

public interface StockTransferRepository {
    void recordTransfer(long itemId, long batchId, String fromLocation, String toLocation, String referenceCode);
}

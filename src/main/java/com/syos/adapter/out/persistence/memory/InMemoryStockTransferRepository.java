package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.StockTransferRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryStockTransferRepository implements StockTransferRepository {
    public static final class TransferRecord {
        public final long itemId;
        public final long batchId;
        public final String fromLocation;
        public final String toLocation;
        public final String referenceCode;
        public TransferRecord(long itemId, long batchId, String fromLocation, String toLocation, String referenceCode) {
            this.itemId = itemId; this.batchId = batchId; this.fromLocation = fromLocation; this.toLocation = toLocation; this.referenceCode = referenceCode;
        }
    }

    private final List<TransferRecord> records = new ArrayList<>();

    @Override
    public void recordTransfer(long itemId, long batchId, String fromLocation, String toLocation, String referenceCode) {
        records.add(new TransferRecord(itemId, batchId, fromLocation, toLocation, referenceCode));
    }

    @Override
    public boolean hasTransferRecord(long itemId, String fromLocation, String toLocation) {
        return records.stream()
            .anyMatch(r -> r.itemId == itemId && 
                          r.fromLocation.equals(fromLocation) && 
                          r.toLocation.equals(toLocation));
    }

    // Test helper
    public List<TransferRecord> getRecords() { return records; }
}

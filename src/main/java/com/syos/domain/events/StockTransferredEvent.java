package com.syos.domain.events;

public final class StockTransferredEvent implements DomainEvent {
    private final long itemId;
    private final long batchId;
    private final String fromLocation;
    private final String toLocation;
    private final String reason;

    public StockTransferredEvent(long itemId, long batchId, String fromLocation, String toLocation, String reason) {
        this.itemId = itemId;
        this.batchId = batchId;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.reason = reason;
    }

    public long getItemId() { return itemId; }
    public long getBatchId() { return batchId; }
    public String getFromLocation() { return fromLocation; }
    public String getToLocation() { return toLocation; }
    public String getReason() { return reason; }
}

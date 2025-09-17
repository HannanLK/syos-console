package com.syos.domain.events;

public final class StockReceivedEvent implements DomainEvent {
    private final long itemId;
    private final long batchId;
    private final String location; // e.g., WAREHOUSE
    private final String reason;

    public StockReceivedEvent(long itemId, long batchId, String location, String reason) {
        this.itemId = itemId;
        this.batchId = batchId;
        this.location = location;
        this.reason = reason;
    }

    public long getItemId() { return itemId; }
    public long getBatchId() { return batchId; }
    public String getLocation() { return location; }
    public String getReason() { return reason; }
}

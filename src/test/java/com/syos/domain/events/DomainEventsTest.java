package com.syos.domain.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainEventsTest {

    @Test
    void stockTransferredEvent_gettersWork() {
        StockTransferredEvent ev = new StockTransferredEvent(1L, 2L, "WAREHOUSE", "SHELF", "Replenishment");
        assertTrue(ev instanceof DomainEvent);
        assertEquals(1L, ev.getItemId());
        assertEquals(2L, ev.getBatchId());
        assertEquals("WAREHOUSE", ev.getFromLocation());
        assertEquals("SHELF", ev.getToLocation());
        assertEquals("Replenishment", ev.getReason());
    }

    @Test
    void stockReceivedEvent_gettersWork() {
        StockReceivedEvent ev = new StockReceivedEvent(5L, 9L, "WAREHOUSE", "Supplier delivery");
        assertTrue(ev instanceof DomainEvent);
        assertEquals(5L, ev.getItemId());
        assertEquals(9L, ev.getBatchId());
        assertEquals("WAREHOUSE", ev.getLocation());
        assertEquals("Supplier delivery", ev.getReason());
    }
}

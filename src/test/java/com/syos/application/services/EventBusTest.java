package com.syos.application.services;

import com.syos.domain.events.DomainEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EventBusTest {

    static class DummyEvent implements DomainEvent { }

    static class CountingSubscriber implements EventBus.EventSubscriber {
        final AtomicInteger handled = new AtomicInteger();
        @Override public boolean canHandle(DomainEvent event) { return event instanceof DummyEvent; }
        @Override public void handle(DomainEvent event) { handled.incrementAndGet(); }
    }

    static class ErroringSubscriber implements EventBus.EventSubscriber {
        @Override public boolean canHandle(DomainEvent event) { return true; }
        @Override public void handle(DomainEvent event) { throw new RuntimeException("boom"); }
    }

    @AfterEach
    void cleanup() {
        EventBus.getInstance().clearSubscribers();
    }

    @Test
    void subscribe_publish_unsubscribe_and_clear_coverAllPaths() {
        EventBus bus = EventBus.getInstance();
        assertNotNull(bus);
        assertEquals(0, bus.getSubscriberCount());

        CountingSubscriber ok = new CountingSubscriber();
        bus.subscribe(ok);
        assertEquals(1, bus.getSubscriberCount());

        // null event should be ignored
        bus.publish(null);
        assertEquals(0, ok.handled.get());

        // publish real event
        bus.publish(new DummyEvent());
        assertEquals(1, ok.handled.get());

        // subscriber that errors should be caught and not affect others
        ErroringSubscriber bad = new ErroringSubscriber();
        bus.subscribe(bad);
        assertEquals(2, bus.getSubscriberCount());
        bus.publish(new DummyEvent());
        // ok still gets it despite bad throwing
        assertEquals(2, ok.handled.get());

        // unsubscribe path
        bus.unsubscribe(ok);
        assertEquals(1, bus.getSubscriberCount());

        // clear path
        bus.clearSubscribers();
        assertEquals(0, bus.getSubscriberCount());
    }

    @Test
    void subscribe_rejectsNull() {
        EventBus bus = EventBus.getInstance();
        assertThrows(IllegalArgumentException.class, () -> bus.subscribe(null));
    }
}

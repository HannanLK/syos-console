package com.syos.application.services;

import com.syos.domain.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event Bus implementation using Observer Pattern (Pattern #12)
 * Provides centralized event publishing and subscription management
 * 
 * Design Patterns:
 * - Observer Pattern: Core event notification mechanism
 * - Singleton Pattern: Ensures single event bus instance
 * 
 * Clean Architecture: Application Layer
 */
public class EventBus {
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
    
    private static volatile EventBus instance;
    private final List<EventSubscriber> subscribers;
    
    private EventBus() {
        this.subscribers = new CopyOnWriteArrayList<>(); // Thread-safe list
    }
    
    /**
     * Get singleton instance of EventBus
     * Double-checked locking for thread safety
     */
    public static EventBus getInstance() {
        if (instance == null) {
            synchronized (EventBus.class) {
                if (instance == null) {
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }
    
    /**
     * Subscribe to events
     * @param subscriber Event subscriber
     */
    public void subscribe(EventSubscriber subscriber) {
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber cannot be null");
        }
        
        subscribers.add(subscriber);
        logger.debug("Subscriber {} registered for events", subscriber.getClass().getSimpleName());
    }
    
    /**
     * Unsubscribe from events
     * @param subscriber Event subscriber to remove
     */
    public void unsubscribe(EventSubscriber subscriber) {
        if (subscriber != null) {
            boolean removed = subscribers.remove(subscriber);
            if (removed) {
                logger.debug("Subscriber {} unregistered from events", subscriber.getClass().getSimpleName());
            }
        }
    }
    
    /**
     * Publish event to all subscribers
     * @param event Domain event to publish
     */
    public void publish(DomainEvent event) {
        if (event == null) {
            logger.warn("Attempted to publish null event");
            return;
        }
        
        logger.debug("Publishing event: {}", event.getClass().getSimpleName());
        
        for (EventSubscriber subscriber : subscribers) {
            try {
                if (subscriber.canHandle(event)) {
                    subscriber.handle(event);
                }
            } catch (Exception e) {
                logger.error("Error handling event {} by subscriber {}", 
                           event.getClass().getSimpleName(), 
                           subscriber.getClass().getSimpleName(), e);
                // Continue with other subscribers even if one fails
            }
        }
        
        logger.debug("Event {} published to {} subscribers", 
                   event.getClass().getSimpleName(), subscribers.size());
    }
    
    /**
     * Get current number of subscribers
     */
    public int getSubscriberCount() {
        return subscribers.size();
    }
    
    /**
     * Clear all subscribers (mainly for testing)
     */
    public void clearSubscribers() {
        int count = subscribers.size();
        subscribers.clear();
        logger.debug("Cleared {} subscribers", count);
    }
    
    /**
     * Interface for event subscribers
     */
    public interface EventSubscriber {
        /**
         * Check if this subscriber can handle the given event
         * @param event Domain event
         * @return true if subscriber can handle the event
         */
        boolean canHandle(DomainEvent event);
        
        /**
         * Handle the domain event
         * @param event Domain event to handle
         */
        void handle(DomainEvent event);
        
        /**
         * Get subscriber priority (higher numbers = higher priority)
         * @return priority level
         */
        default int getPriority() {
            return 0;
        }
    }
}

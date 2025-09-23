package com.syos.domain.entities;

import com.syos.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WebInventoryEntityTest {

    private static ItemCode code() { return ItemCode.of("ITM-WEB-1"); }
    private static Quantity qty(int v) { return Quantity.of(BigDecimal.valueOf(v)); }
    private static Money money(double v) { return Money.of(v); }
    private static UserID uid(long v) { return UserID.of(v); }

    private static WebInventory web(int quantity, LocalDateTime expiry) {
        return WebInventory.createNew(code(), 1L, 11L, qty(quantity), expiry, uid(9L), money(199.99));
    }

    @Test
    void create_sell_restock_price_publish_featured_content_and_helpers() {
        WebInventory w = web(50, LocalDateTime.now().plusDays(10));
        assertTrue(w.isPublished());
        assertFalse(w.isFeatured());
        assertTrue(w.isAvailableForPurchase());
        assertFalse(w.isExpired());
        assertFalse(w.isExpiringSoon());
        assertFalse(w.isLowStock());
        assertNotNull(w.getTotalValue());

        // expiring soon path
        WebInventory soon = new WebInventory.Builder(w)
                .expiryDate(LocalDateTime.now().plusDays(2))
                .lastUpdatedBy(uid(1L)).build();
        assertTrue(soon.isExpiringSoon());

        // sell success and failure
        WebInventory afterSell = w.sellStock(qty(3), uid(2L));
        assertEquals(new BigDecimal("47"), afterSell.getQuantityAvailable().toBigDecimal());
        assertThrows(IllegalArgumentException.class, () -> w.sellStock(qty(99), uid(2L)));

        // restock success and failure
        WebInventory restocked = w.restockWeb(qty(5), uid(3L));
        assertEquals(new BigDecimal("55"), restocked.getQuantityAvailable().toBigDecimal());
        assertThrows(IllegalArgumentException.class, () -> w.restockWeb(Quantity.zero(), uid(3L)));

        // price update success/failure
        WebInventory priced = w.updateWebPrice(money(250), uid(4L));
        assertEquals(money(250), priced.getWebPrice());
        assertThrows(IllegalArgumentException.class, () -> w.updateWebPrice(money(0), uid(4L)));

        // publish/feature toggles and content updates
        WebInventory unpublished = w.setPublishStatus(false, uid(5L));
        assertFalse(unpublished.isPublished());
        WebInventory featured = w.setFeaturedStatus(true, uid(6L));
        assertTrue(featured.isFeatured());
        WebInventory content = w.updateWebContent("desc", "milk, 1L", uid(7L));
        assertEquals("desc", content.getWebDescription());
        assertEquals("milk, 1L", content.getSeoKeywords());
    }

    @Test
    void builder_validation_rules_and_stockLevelBounds() {
        // stockLevel out of bounds
        assertThrows(IllegalArgumentException.class, () ->
                new WebInventory.Builder()
                        .itemCode(code()).itemId(1L).batchId(2L)
                        .quantityAvailable(qty(1))
                        .addedBy(uid(1L))
                        .webPrice(money(1))
                        .stockLevel(101) // invalid
                        .lastUpdatedBy(uid(1L))
                        .build());

        // negative quantity
        assertThrows(IllegalArgumentException.class, () ->
                new WebInventory.Builder()
                        .itemCode(code()).itemId(1L).batchId(2L)
                        .quantityAvailable(Quantity.of(new BigDecimal("-1")))
                        .addedBy(uid(1L))
                        .webPrice(money(1))
                        .lastUpdatedBy(uid(1L))
                        .build());

        // non-positive price
        assertThrows(IllegalArgumentException.class, () ->
                new WebInventory.Builder()
                        .itemCode(code()).itemId(1L).batchId(2L)
                        .quantityAvailable(qty(1))
                        .addedBy(uid(1L))
                        .webPrice(money(0))
                        .lastUpdatedBy(uid(1L))
                        .build());

        // equality/hashCode based on itemCode+batchId
        WebInventory a = web(5, LocalDateTime.now().plusDays(5));
        WebInventory b = new WebInventory.Builder(a).lastUpdatedBy(uid(1L)).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}

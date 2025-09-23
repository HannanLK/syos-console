package com.syos.application.dto.responses;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductResponseTest {

    @Test
    void success_withDetails_andToString() {
        ProductResponse r = ProductResponse.success(5L, "ok", "ITM01", "Milk");
        assertTrue(r.isSuccess());
        assertEquals("ok", r.getMessage());
        assertEquals(5L, r.getProductId());
        assertEquals("ITM01", r.getItemCode());
        assertEquals("Milk", r.getItemName());
        assertNull(r.getError());
        assertFalse(r.isFailure());
        assertTrue(r.toString().contains("success=true"));
    }

    @Test
    void success_messageOnly_branch() {
        ProductResponse r = ProductResponse.success("created");
        assertTrue(r.isSuccess());
        assertEquals("created", r.getMessage());
        assertNull(r.getProductId());
        assertNull(r.getItemCode());
        assertNull(r.getItemName());
    }

    @Test
    void failure_withError_and_branch_withItemCode() {
        ProductResponse f1 = ProductResponse.failure("bad data");
        assertFalse(f1.isSuccess());
        assertTrue(f1.isFailure());
        assertEquals("bad data", f1.getError());
        assertNull(f1.getItemCode());
        assertTrue(f1.toString().contains("success=false"));

        ProductResponse f2 = ProductResponse.failure("missing brand", "ITM02");
        assertFalse(f2.isSuccess());
        assertEquals("missing brand", f2.getError());
        assertEquals("ITM02", f2.getItemCode());
        assertNull(f2.getItemName());
    }
}

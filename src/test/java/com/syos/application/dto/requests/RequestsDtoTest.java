package com.syos.application.dto.requests;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RequestsDtoTest {

    @Test
    void productRequest_validationAndStockFlags() {
        ProductRequest req = new ProductRequest(
                "ITM-001", "Milk", "Fresh milk",
                1L, 2L, 3L,
                "L", 1.0,
                120.0, 150.0, 25,
                true
        );
        assertTrue(req.isValid());
        assertFalse(req.hasInitialStock());

        req.setBatchNumber("B-1");
        req.setInitialQuantity(50);
        req.setManufactureDate(LocalDate.of(2025, 1, 1));
        req.setExpiryDate(LocalDateTime.of(2025, 12, 31, 23, 59));
        req.setTransferToShelf(true);
        req.setTransferToWeb(true);
        req.setShelfCode("A1");
        req.setShelfQuantity(10);
        req.setWebQuantity(20);

        assertTrue(req.hasInitialStock());
        assertEquals("ITM-001", req.getItemCode());
        assertEquals("Milk", req.getItemName());
        assertEquals(1L, req.getBrandId());
        assertEquals("MAIN-WAREHOUSE", req.getWarehouseLocation());
        assertTrue(req.isTransferToShelf());
        assertTrue(req.isTransferToWeb());
        assertEquals(10, req.getShelfQuantity());
        assertEquals(20, req.getWebQuantity());
        assertNotNull(req.toString());
    }

    @Test
    void productRequest_invalidWhenMissingRequiredFields() {
        ProductRequest req = new ProductRequest();
        req.setItemCode("");
        req.setItemName(null);
        req.setCostPrice(-1);
        req.setSellingPrice(0);
        req.setReorderPoint(-5);
        assertFalse(req.isValid());
    }

    @Test
    void registerRequest_constructorsAndFactory() {
        RegisterRequest r1 = new RegisterRequest("john", "secret", "John", "j@mail");
        assertEquals("john", r1.getUsername());
        assertEquals("secret", r1.getPassword());
        assertEquals("John", r1.getName());
        assertEquals("j@mail", r1.getEmail());

        RegisterRequest r2 = RegisterRequest.fromLegacy("John", "john", "j@mail", "secret");
        assertEquals("john", r2.getUsername());
        assertEquals("secret", r2.getPassword());
        assertEquals("John", r2.getName());
        assertEquals("j@mail", r2.getEmail());
    }

    @Test
    void loginRequest_constructorAndGetters() {
        LoginRequest l = new LoginRequest("john", "secret");
        assertEquals("john", l.getUsername());
        assertEquals("secret", l.getPassword());
    }
}

package com.syos.application.dto.responses;

import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseTest {

    @Test
    void successFactory_withDefaultMessageAndAllFields() {
        LocalDateTime now = LocalDateTime.now();
        UserResponse r = UserResponse.success(1L, "john", "John", "j@mail", UserRole.CUSTOMER, "10 points", true, now);
        assertTrue(r.isSuccess());
        assertEquals(1L, r.getUserId());
        assertEquals("john", r.getUsername());
        assertEquals("John", r.getName());
        assertEquals("j@mail", r.getEmail());
        assertEquals(UserRole.CUSTOMER, r.getRole());
        assertEquals("10 points", r.getSynexPoints());
        assertTrue(r.isActive());
        assertEquals(now, r.getCreatedAt());
        assertNull(r.getErrorMessage());
        assertTrue(r.toString().contains("john"));
    }

    @Test
    void successFactory_withCustomMessage() {
        LocalDateTime now = LocalDateTime.now();
        UserResponse r = UserResponse.success("ok", 2L, "emp", "Emp", "e@mail", UserRole.EMPLOYEE, "0 points", true, now);
        assertTrue(r.isSuccess());
        assertEquals("ok", r.getMessage());
        assertEquals(UserRole.EMPLOYEE, r.getRole());
    }

    @Test
    void successFactory_simpleVariant_populatesAndSetsDefaults() {
        UserResponse r = UserResponse.success(3L, "boss", "Boss", "b@mail", UserRole.ADMIN, "created");
        assertTrue(r.isSuccess());
        assertEquals("created", r.getMessage());
        assertEquals(3L, r.getUserId());
        assertEquals("boss", r.getUsername());
        assertEquals(UserRole.ADMIN, r.getRole());
        assertTrue(r.isActive());
        assertNotNull(r.getCreatedAt());
    }

    @Test
    void failureFactory_setsFailureAndErrorMessage() {
        UserResponse r = UserResponse.failure("bad");
        assertFalse(r.isSuccess());
        assertEquals("bad", r.getMessage());
        assertEquals("bad", r.getErrorMessage());
        assertNull(r.getUserId());
        assertNull(r.getUsername());
        assertFalse(r.isActive());
        assertNull(r.getCreatedAt());
    }
}

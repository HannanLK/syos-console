package com.syos.application.dto.responses;

import com.syos.shared.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void success_defaultMessage_andGetters_andToString() {
        AuthResponse r = AuthResponse.success("sess-1", 10L, "john", UserRole.CUSTOMER);
        assertTrue(r.isSuccess());
        assertEquals("Authentication successful", r.getMessage());
        assertEquals("sess-1", r.getSessionId());
        assertEquals("sess-1", r.getSessionToken());
        assertEquals(10L, r.getUserId());
        assertEquals("john", r.getUsername());
        assertEquals(UserRole.CUSTOMER, r.getRole());
        assertEquals(UserRole.CUSTOMER, r.getUserRole());
        assertNull(r.getToken());
        assertNull(r.getErrorMessage());
        assertTrue(r.toString().contains("john"));
    }

    @Test
    void success_customMessage_branch() {
        AuthResponse r = AuthResponse.success("Welcome!", "sess-2", 20L, "emp", UserRole.EMPLOYEE);
        assertTrue(r.isSuccess());
        assertEquals("Welcome!", r.getMessage());
        assertEquals("sess-2", r.getSessionId());
        assertEquals(20L, r.getUserId());
        assertEquals("emp", r.getUsername());
        assertEquals(UserRole.EMPLOYEE, r.getRole());
    }

    @Test
    void failure_branch_andErrorGetter() {
        AuthResponse r = AuthResponse.failure("bad credentials");
        assertFalse(r.isSuccess());
        assertEquals("bad credentials", r.getMessage());
        assertNull(r.getSessionId());
        assertNull(r.getUserId());
        assertNull(r.getUsername());
        assertNull(r.getRole());
        assertEquals("bad credentials", r.getErrorMessage());
        assertTrue(r.toString().contains("success=false"));
    }
}

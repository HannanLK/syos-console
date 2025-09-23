package com.syos.shared.constants;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class ConstantsTest {

    private static void assertNonInstantiable(Class<?> clazz) throws Exception {
        Constructor<?> ctor = clazz.getDeclaredConstructor();
        ctor.setAccessible(true);
        Throwable t = assertThrows(Throwable.class, ctor::newInstance);
        Throwable cause = (t instanceof java.lang.reflect.InvocationTargetException) ? t.getCause() : t;
        assertTrue(cause instanceof AssertionError);
        assertTrue(cause.getMessage().toLowerCase().contains("constants"));
    }

    @Test
    void applicationConstants_coverage_and_sample_values() throws Exception {
        assertEquals("Synex Outlet Store", ApplicationConstants.APPLICATION_NAME);
        assertEquals("SYOS", ApplicationConstants.STORE_ACRONYM);
        assertTrue(ApplicationConstants.ENABLE_WEB_TRANSACTIONS);
        assertEquals("0767600730204128", ApplicationConstants.FAILED_CARD_NUMBER);
        assertNonInstantiable(ApplicationConstants.class);
    }

    @Test
    void businessConstants_coverage_and_sample_values() throws Exception {
        assertEquals("FIFO", BusinessConstants.STOCK_SELECTION_FIFO);
        assertTrue(BusinessConstants.MAX_TRANSFER_QUANTITY > 0);
        assertTrue(BusinessConstants.ACCEPTED_DENOMINATIONS.length > 0);
        assertTrue(BusinessConstants.RETURNABLE_CATEGORIES.length > 0);
        assertNonInstantiable(BusinessConstants.class);
    }
}

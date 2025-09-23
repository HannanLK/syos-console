package com.syos.infrastructure.persistence.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BillEntityTest {

    private TransactionEntity sampleTransaction() {
        TransactionEntity tx = new TransactionEntity(1L,
                TransactionEntity.TransactionType.POS,
                new BigDecimal("100.00"),
                TransactionEntity.PaymentMethod.CASH);
        tx.setDiscountAmount(new BigDecimal("5.00"));
        tx.setCashTendered(new BigDecimal("200.00"));
        tx.setChangeAmount(new BigDecimal("100.00"));
        tx.setSynexPointsAwarded(1);
        tx.setBillSerialNumber("SYOS0000001");
        tx.setTransactionDate(LocalDateTime.now());
        return tx;
    }

    @Test
    void constructor_populatesFromTransaction_andBusinessHelpersWork() {
        TransactionEntity tx = sampleTransaction();
        BillEntity bill = new BillEntity("SYOS0000001", tx);
        bill.setCustomerName("John");

        assertNotNull(bill.getCreatedAt());
        assertNotNull(bill.getBillDate());
        assertEquals("SYOS0000001", bill.getBillSerialNumber());
        assertEquals("John", bill.getCustomerName());
        assertSame(tx, bill.getTransaction());

        assertEquals(new BigDecimal("100.00"), bill.getTotalAmount());
        assertEquals(new BigDecimal("5.00"), bill.getDiscountAmount());
        assertEquals(new BigDecimal("200.00"), bill.getCashTendered());
        assertEquals(new BigDecimal("100.00"), bill.getChangeAmount());
        assertEquals(1, bill.getSynexPointsAwarded());

        // getFinalAmount subtracts discount
        assertEquals(new BigDecimal("95.00"), bill.getFinalAmount());
        // hasPdfStored initially false
        assertFalse(bill.hasPdfStored());
        bill.setPdfContent(new byte[]{1,2,3});
        assertTrue(bill.hasPdfStored());
        assertNotNull(bill.toString());
    }

    @Test
    void settersAndGetters_allowMutationOfOptionalFields() {
        BillEntity bill = new BillEntity();
        bill.setBillId(10L);
        bill.setBillSerialNumber("EMP0000002");
        bill.setCustomerName("Emp");
        bill.setPdfFilePath("/tmp/bill.pdf");
        bill.setDiscountAmount(null);
        bill.setTotalAmount(new BigDecimal("50.00"));

        assertEquals(10L, bill.getBillId());
        assertEquals("EMP0000002", bill.getBillSerialNumber());
        assertEquals("Emp", bill.getCustomerName());
        assertEquals("/tmp/bill.pdf", bill.getPdfFilePath());
        // If discount is null, final amount equals total
        assertEquals(new BigDecimal("50.00"), bill.getFinalAmount());
    }
}

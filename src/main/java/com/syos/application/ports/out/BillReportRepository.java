package com.syos.application.ports.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only reporting repository for Bills.
 * Returns lightweight projections (Object[] rows) to keep dependencies minimal.
 */
public interface BillReportRepository {
    /**
     * Bills between a range [start, end).
     * Rows: [String billSerial, java.time.LocalDateTime billDate, String channel, java.math.BigDecimal totalAmount, String customerName]
     */
    List<Object[]> listBillsBetween(LocalDateTime startInclusive, LocalDateTime endExclusive);

    /**
     * Recent bills limited by count.
     * Rows: [String billSerial, java.time.LocalDateTime billDate, String channel, java.math.BigDecimal totalAmount, String customerName]
     */
    List<Object[]> listRecentBills(int limit);

    /**
     * Bills for a specific date.
     * Rows: [String billSerial, java.time.LocalDateTime billDate, String channel, java.math.BigDecimal totalAmount, String customerName]
     */
    List<Object[]> listBillsForDate(LocalDate date);
}

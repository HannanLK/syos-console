package com.syos.application.ports.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only reporting repository for Transactions and their items.
 * Returns lightweight projections (Object[] rows) to keep dependencies minimal.
 * Row formats are documented per method.
 */
public interface TransactionReportRepository {
    /**
     * Daily summary for a specific date.
     * Returns a single row: [Long txCount, java.math.BigDecimal totalAmount, java.math.BigDecimal totalDiscount]
     */
    Object[] findDailySummary(LocalDate date);

    /**
     * Item aggregates for a specific date.
     * Rows: [String itemCode, String itemName, Long quantity, java.math.BigDecimal revenue]
     */
    List<Object[]> findDailyItemAggregates(LocalDate date);

    /**
     * Channel breakdown between start (inclusive) and end (exclusive).
     * Rows: [String channel, Long txCount, java.math.BigDecimal totalAmount, java.math.BigDecimal avgOrderValue]
     */
    List<Object[]> findChannelSummary(LocalDateTime startInclusive, LocalDateTime endExclusive);
}

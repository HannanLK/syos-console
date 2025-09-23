package com.syos.application.ports.out;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BillReportRepositoryTest {

    static class InMemoryBillReportRepository implements BillReportRepository {
        private final List<Object[]> rows = new ArrayList<>();

        void add(String serial, LocalDateTime date, String channel, BigDecimal total, String customer) {
            rows.add(new Object[]{serial, date, channel, total, customer});
        }

        @Override
        public List<Object[]> listBillsBetween(LocalDateTime startInclusive, LocalDateTime endExclusive) {
            List<Object[]> out = new ArrayList<>();
            for (Object[] r : rows) {
                LocalDateTime d = (LocalDateTime) r[1];
                if ((d.isEqual(startInclusive) || d.isAfter(startInclusive)) && d.isBefore(endExclusive)) {
                    out.add(r);
                }
            }
            return out;
        }

        @Override
        public List<Object[]> listRecentBills(int limit) {
            List<Object[]> out = new ArrayList<>();
            int start = Math.max(0, rows.size() - limit);
            for (int i = start; i < rows.size(); i++) {
                out.add(rows.get(i));
            }
            return out;
        }

        @Override
        public List<Object[]> listBillsForDate(LocalDate date) {
            List<Object[]> out = new ArrayList<>();
            for (Object[] r : rows) {
                LocalDateTime d = (LocalDateTime) r[1];
                if (d.toLocalDate().equals(date)) {
                    out.add(r);
                }
            }
            return out;
        }
    }

    @Test
    void repository_methodsReturnExpectedShapesAndFiltering() {
        InMemoryBillReportRepository repo = new InMemoryBillReportRepository();
        LocalDateTime now = LocalDateTime.now();
        repo.add("SYOS00001", now.minusDays(2), "POS", new BigDecimal("100.00"), "Alice");
        repo.add("SYOS00002", now.minusDays(1), "WEB", new BigDecimal("200.00"), "Bob");
        repo.add("SYOS00003", now, "POS", new BigDecimal("300.00"), "Cara");

        // between
        List<Object[]> between = repo.listBillsBetween(now.minusDays(1), now.plusDays(1));
        assertEquals(2, between.size());
        assertArrayMatchesShape(between.get(0));

        // recent limit
        List<Object[]> recent = repo.listRecentBills(2);
        assertEquals(2, recent.size());
        assertEquals("SYOS00002", recent.get(0)[0]);
        assertEquals("SYOS00003", recent.get(1)[0]);

        // by date
        List<Object[]> byDate = repo.listBillsForDate(now.toLocalDate());
        assertEquals(1, byDate.size());
        assertEquals("SYOS00003", byDate.get(0)[0]);
    }

    private static void assertArrayMatchesShape(Object[] row) {
        assertEquals(5, row.length);
        assertTrue(row[0] instanceof String); // billSerial
        assertTrue(row[1] instanceof LocalDateTime); // billDate
        assertTrue(row[2] instanceof String); // channel
        assertTrue(row[3] instanceof BigDecimal); // totalAmount
        assertTrue(row[4] instanceof String); // customerName
    }
}

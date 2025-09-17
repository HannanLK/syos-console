package com.syos.application.strategies.stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implements FIFO with Expiry Override:
 * - Primary rule: FIFO by receivedDate (oldest first)
 * - Override: If a newer batch expires earlier than the oldest batch, dispatch the earlier-expiring batch first.
 *
 * This algorithm works iteratively so the override decision is re-evaluated at each allocation step,
 * ensuring consistent behavior across mixed perishable/non-perishable batches.
 */
public class FIFOWithExpiryStrategy implements StockSelectionStrategy {

    @Override
    public List<BatchAllocation> selectBatchesForDispatch(List<BatchInfo> batches, BigDecimal requiredQuantity) {
        Objects.requireNonNull(batches, "batches");
        Objects.requireNonNull(requiredQuantity, "requiredQuantity");
        if (requiredQuantity.signum() <= 0) throw new IllegalArgumentException("requiredQuantity must be > 0");

        // Work on a copy and normalize quantities
        final List<BatchInfo> pool = new ArrayList<>(batches);
        // Sort FIFO as baseline to simplify oldest lookup
        pool.sort(Comparator.comparing(BatchInfo::getReceivedDate));

        BigDecimal remaining = requiredQuantity;
        List<BatchAllocation> allocations = new ArrayList<>();

        while (remaining.signum() > 0) {
            // Find the first batch with available qty > 0
            Optional<BatchInfo> oldestOpt = pool.stream()
                    .filter(b -> b.getAvailableQuantity().signum() > 0)
                    .findFirst();
            if (oldestOpt.isEmpty()) break; // no stock

            BatchInfo oldest = oldestOpt.get();
            LocalDate oldestExpiry = oldest.getExpiryDate();
            LocalDate oldestExpiryOrMax = oldestExpiry != null ? oldestExpiry : LocalDate.MAX;

            // Expiry override: among newer batches, pick the one with earliest expiry earlier than oldest's expiry
            Optional<BatchInfo> overrideOpt = pool.stream()
                    .filter(b -> b.getAvailableQuantity().signum() > 0)
                    .filter(b -> b.getExpiryDate() != null)
                    .filter(b -> b.getExpiryDate().isBefore(oldestExpiryOrMax))
                    .min(Comparator.comparing(BatchInfo::getExpiryDate));

            BatchInfo chosen = overrideOpt.orElse(oldest);

            // Allocate from chosen batch
            BigDecimal take = min(remaining, chosen.getAvailableQuantity());
            if (take.signum() <= 0) {
                // Defensive: if zero due to rounding or bad data, stop to avoid infinite loop
                break;
            }
            allocations.add(BatchAllocation.of(chosen.getBatchId(), take));

            // Reduce remaining and mutate a local mirror of available qty via replacement object
            remaining = remaining.subtract(take);
            replaceBatchWithReducedQty(pool, chosen, take);
        }

        return allocations;
    }

    private static BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) <= 0 ? a : b;
    }

    private static void replaceBatchWithReducedQty(List<BatchInfo> pool, BatchInfo chosen, BigDecimal take) {
        int idx = pool.indexOf(chosen);
        if (idx < 0) return; // should not happen
        BigDecimal remaining = chosen.getAvailableQuantity().subtract(take);
        pool.set(idx, BatchInfo.of(
                chosen.getBatchId(),
                remaining.max(BigDecimal.ZERO),
                chosen.getReceivedDate(),
                chosen.getExpiryDate()
        ));
    }
}

package no.ecovision.dashboard;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response for GET /api/dashboard/summary (ARCHITECTURE.md section 7).
 *
 * <p>{@code totalKg} is the running total over the requested window, {@code byCategory}
 * the breakdown behind it, and {@code dailySeries} the same total sliced by day for the
 * trend chart. Every figure is aggregated in SQL from the {@code emissions_kg} frozen
 * onto each activity_log row - never recomputed from current factors.
 */
public record DashboardSummaryResponse(
        BigDecimal totalKg,
        List<CategoryEmission> byCategory,
        List<DailyEmission> dailySeries) {
}

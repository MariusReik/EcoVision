package no.ecovision.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One point on the dashboard trend line: summed kg CO2e for every activity that
 * occurred on that date. Days with no activity are absent rather than zero-filled;
 * the frontend decides how to render gaps.
 */
public record DailyEmission(LocalDate date, BigDecimal totalKg) {
}

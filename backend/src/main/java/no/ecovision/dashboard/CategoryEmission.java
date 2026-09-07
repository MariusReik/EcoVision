package no.ecovision.dashboard;

import java.math.BigDecimal;

import no.ecovision.emission.ActivityCategory;

/** One row of the dashboard's by-category breakdown: summed kg CO2e for that category. */
public record CategoryEmission(ActivityCategory category, BigDecimal totalKg) {
}

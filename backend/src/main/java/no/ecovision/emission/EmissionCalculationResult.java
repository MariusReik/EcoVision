package no.ecovision.emission;

import java.math.BigDecimal;

/** The resolved factor id and the computed emissions, ready to be frozen onto an activity_log row. */
public record EmissionCalculationResult(Long emissionFactorId, BigDecimal emissionsKg) {
}

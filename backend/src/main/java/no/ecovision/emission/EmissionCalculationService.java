package no.ecovision.emission;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Computes emissions for a single activity. Never called at read time: the result
 * is written once onto activity_log and never recomputed. See ARCHITECTURE.md
 * section 5, decision 1.
 */
@Service
public class EmissionCalculationService {

    // Matches emission_factor.emissions_kg column: NUMERIC(14, 4).
    private static final int EMISSIONS_SCALE = 4;

    private final EmissionFactorRepository emissionFactorRepository;

    public EmissionCalculationService(EmissionFactorRepository emissionFactorRepository) {
        this.emissionFactorRepository = emissionFactorRepository;
    }

    @Transactional(readOnly = true)
    public EmissionCalculationResult calculate(
            String activityTypeCode,
            String region,
            AccountingBasis basis,
            LocalDate occurredOn,
            BigDecimal quantity) {

        EmissionFactor factor = emissionFactorRepository
                .resolve(activityTypeCode, region, basis, occurredOn)
                .orElseThrow(() -> new FactorNotFoundException(activityTypeCode, region, basis, occurredOn));

        BigDecimal emissionsKg = quantity
                .multiply(factor.getFactorKgCo2e())
                .setScale(EMISSIONS_SCALE, RoundingMode.HALF_UP);

        return new EmissionCalculationResult(factor.getId(), emissionsKg);
    }
}

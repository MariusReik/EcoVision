package no.ecovision.emission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ports scenarios 1-3 from "Schema verification.sql" into JUnit against a real
 * Postgres instance, plus one case explicitly asserting that resolution for a
 * historical date is unaffected by a later factor reissue.
 *
 * <p>The emission_factor rows below are test-only fixtures, not seeded production
 * data - V2__seed_reference_data.sql is deliberately still empty pending cited
 * sources (CLAUDE.md: never invent an emission factor). Their values mirror the
 * scenarios in "Schema verification.sql" so this stays a faithful port of that file.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class EmissionCalculationServiceTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ecovision")
            .withUsername("ecovision")
            .withPassword("ecovision");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    private static final String ELECTRICITY = "electricity";

    @Autowired
    private EmissionCalculationService emissionCalculationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long gb2025FactorId;
    private Long gb2026FactorId;

    @BeforeEach
    void seedFixtures() {
        jdbcTemplate.update("DELETE FROM emission_factor");
        jdbcTemplate.update("DELETE FROM activity_type");

        jdbcTemplate.update("""
                INSERT INTO activity_type (code, category, display_name, unit)
                VALUES (?, 'ENERGY', 'Electricity', 'kWh')
                """, ELECTRICITY);

        // Test 1 (temporal versioning): same region, two non-overlapping windows.
        gb2025FactorId = insertFactor(ELECTRICITY, "GB", "LOCATION", "0.177",
                "TEST FIXTURE - DEFRA-style GB grid factor", 2025,
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 6, 1));
        gb2026FactorId = insertFactor(ELECTRICITY, "GB", "LOCATION", "0.131",
                "TEST FIXTURE - DEFRA-style GB grid factor (2026 reissue)", 2026,
                LocalDate.of(2026, 6, 1), null);

        // Test 2 (regional match beats GLOBAL fallback).
        insertFactor(ELECTRICITY, "NO", "LOCATION", "0.028",
                "TEST FIXTURE - NVE-style Norway physical mix", 2025,
                LocalDate.of(2025, 1, 1), null);
        insertFactor(ELECTRICITY, "GLOBAL", "LOCATION", "0.445",
                "TEST FIXTURE - global average grid factor", 2025,
                LocalDate.of(2025, 1, 1), null);
    }

    private Long insertFactor(String typeCode, String region, String basis, String factor,
                               String source, int sourceYear, LocalDate validFrom, LocalDate validTo) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO emission_factor
                    (activity_type_code, region, accounting_basis, factor_kg_co2e,
                     source, source_year, valid_from, valid_to)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                typeCode, region, basis, new BigDecimal(factor), source, sourceYear, validFrom, validTo);
    }

    @Test
    void temporalVersioning_resolvesTheFactorValidOnTheGivenDate() {
        var beforeReissue = emissionCalculationService.calculate(
                ELECTRICITY, "GB", AccountingBasis.LOCATION, LocalDate.of(2025, 3, 15), BigDecimal.ONE);
        assertThat(beforeReissue.emissionFactorId()).isEqualTo(gb2025FactorId);
        assertThat(beforeReissue.emissionsKg()).isEqualByComparingTo("0.177");

        var atReissueBoundary = emissionCalculationService.calculate(
                ELECTRICITY, "GB", AccountingBasis.LOCATION, LocalDate.of(2026, 6, 1), BigDecimal.ONE);
        assertThat(atReissueBoundary.emissionFactorId()).isEqualTo(gb2026FactorId);
        assertThat(atReissueBoundary.emissionsKg()).isEqualByComparingTo("0.131");
    }

    @Test
    void regionalMatchBeatsGlobalFallback() {
        var norway = emissionCalculationService.calculate(
                ELECTRICITY, "NO", AccountingBasis.LOCATION, LocalDate.of(2026, 6, 1), BigDecimal.ONE);
        assertThat(norway.emissionsKg()).isEqualByComparingTo("0.028");

        var germany = emissionCalculationService.calculate(
                ELECTRICITY, "DE", AccountingBasis.LOCATION, LocalDate.of(2026, 6, 1), BigDecimal.ONE);
        assertThat(germany.emissionsKg()).isEqualByComparingTo("0.445");
    }

    @Test
    void dateBeforeAnyFactorExists_throwsFactorNotFoundException() {
        LocalDate date = LocalDate.of(2019, 1, 1);

        assertThatThrownBy(() -> emissionCalculationService.calculate(
                ELECTRICITY, "GB", AccountingBasis.LOCATION, date, BigDecimal.ONE))
                .isInstanceOf(FactorNotFoundException.class)
                .satisfies(ex -> {
                    var notFound = (FactorNotFoundException) ex;
                    assertThat(notFound.getTypeCode()).isEqualTo(ELECTRICITY);
                    assertThat(notFound.getRegion()).isEqualTo("GB");
                    assertThat(notFound.getDate()).isEqualTo(date);
                });
    }

    @Test
    void historicalActivity_stillResolvesTheOldFactor_afterTheNewerFactorExists() {
        // The 2026 GB reissue (gb2026FactorId) already exists in the table at this
        // point. A 2025-dated activity must still resolve the pre-reissue factor -
        // the whole point of storing valid_from/valid_to is that history doesn't move.
        var result = emissionCalculationService.calculate(
                ELECTRICITY, "GB", AccountingBasis.LOCATION, LocalDate.of(2025, 3, 15), BigDecimal.ONE);

        assertThat(result.emissionFactorId()).isEqualTo(gb2025FactorId);
        assertThat(result.emissionFactorId()).isNotEqualTo(gb2026FactorId);
        assertThat(result.emissionsKg()).isEqualByComparingTo("0.177");
    }

    @Test
    void emissionsAreQuantityTimesFactor_roundedToFourDecimalPlaces() {
        var result = emissionCalculationService.calculate(
                ELECTRICITY, "GB", AccountingBasis.LOCATION, LocalDate.of(2025, 3, 15), new BigDecimal("42.5"));

        // 42.5 * 0.177 = 7.5225
        assertThat(result.emissionsKg()).isEqualByComparingTo("7.5225");
    }
}

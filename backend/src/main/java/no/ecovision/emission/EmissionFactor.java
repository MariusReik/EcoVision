package no.ecovision.emission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Versioned, region-scoped conversion factor. A row is valid over
 * [validFrom, validTo), with validTo null meaning "current". Matches
 * V1__baseline.sql exactly; if they disagree, fix this entity, not the migration.
 */
@Entity
@Table(name = "emission_factor")
public class EmissionFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_type_code", referencedColumnName = "code", nullable = false)
    private ActivityType activityType;

    @Column(name = "region", length = 10, nullable = false)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(name = "accounting_basis", length = 10)
    private AccountingBasis accountingBasis;

    @Column(name = "factor_kg_co2e", nullable = false, precision = 12, scale = 6)
    private BigDecimal factorKgCo2e;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "source_year", nullable = false)
    private Integer sourceYear;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected EmissionFactor() {
        // JPA
    }

    public Long getId() {
        return id;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public String getRegion() {
        return region;
    }

    public AccountingBasis getAccountingBasis() {
        return accountingBasis;
    }

    public BigDecimal getFactorKgCo2e() {
        return factorKgCo2e;
    }

    public String getSource() {
        return source;
    }

    public Integer getSourceYear() {
        return sourceYear;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

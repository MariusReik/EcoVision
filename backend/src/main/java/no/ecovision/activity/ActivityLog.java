package no.ecovision.activity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * One logged activity. emissionFactorId and emissionsKg are written once, at insert
 * time, and never recomputed - see ARCHITECTURE.md section 5, decision 1. Matches
 * V1__baseline.sql exactly; if they disagree, fix this entity, not the migration.
 */
@Entity
@Table(name = "activity_log")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "activity_type_code", nullable = false, length = 50)
    private String activityTypeCode;

    @Column(name = "quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "occurred_on", nullable = false)
    private LocalDate occurredOn;

    @Column(name = "emission_factor_id", nullable = false)
    private Long emissionFactorId;

    @Column(name = "emissions_kg", nullable = false, precision = 14, scale = 4)
    private BigDecimal emissionsKg;

    @Column(name = "note", length = 280)
    private String note;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected ActivityLog() {
        // JPA
    }

    public ActivityLog(
            UUID userId,
            String activityTypeCode,
            BigDecimal quantity,
            LocalDate occurredOn,
            Long emissionFactorId,
            BigDecimal emissionsKg,
            String note) {
        this.userId = userId;
        this.activityTypeCode = activityTypeCode;
        this.quantity = quantity;
        this.occurredOn = occurredOn;
        this.emissionFactorId = emissionFactorId;
        this.emissionsKg = emissionsKg;
        this.note = note;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getActivityTypeCode() {
        return activityTypeCode;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public LocalDate getOccurredOn() {
        return occurredOn;
    }

    public Long getEmissionFactorId() {
        return emissionFactorId;
    }

    public BigDecimal getEmissionsKg() {
        return emissionsKg;
    }

    public String getNote() {
        return note;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

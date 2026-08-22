package no.ecovision.emission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Reference table: what a user can log, and in what unit. Matches V1__baseline.sql. */
@Entity
@Table(name = "activity_type")
public class ActivityType {

    @Id
    @Column(name = "code", length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30, nullable = false)
    private ActivityCategory category;

    @Column(name = "display_name", length = 100, nullable = false)
    private String displayName;

    @Column(name = "unit", length = 20, nullable = false)
    private String unit;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected ActivityType() {
        // JPA
    }

    public ActivityType(String code, ActivityCategory category, String displayName, String unit) {
        this.code = code;
        this.category = category;
        this.displayName = displayName;
        this.unit = unit;
        this.sortOrder = 0;
        this.active = true;
    }

    public String getCode() {
        return code;
    }

    public ActivityCategory getCategory() {
        return category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isActive() {
        return active;
    }
}

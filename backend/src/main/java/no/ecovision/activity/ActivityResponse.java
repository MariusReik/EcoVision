package no.ecovision.activity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        String activityTypeCode,
        BigDecimal quantity,
        LocalDate occurredOn,
        BigDecimal emissionsKg,
        String note,
        OffsetDateTime createdAt) {

    public static ActivityResponse from(ActivityLog activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getActivityTypeCode(),
                activity.getQuantity(),
                activity.getOccurredOn(),
                activity.getEmissionsKg(),
                activity.getNote(),
                activity.getCreatedAt());
    }
}

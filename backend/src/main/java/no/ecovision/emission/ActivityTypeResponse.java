package no.ecovision.emission;

/** GET /api/activity-types item. See ARCHITECTURE.md section 7. */
public record ActivityTypeResponse(String code, ActivityCategory category, String unit, String displayName) {

    public static ActivityTypeResponse from(ActivityType activityType) {
        return new ActivityTypeResponse(
                activityType.getCode(),
                activityType.getCategory(),
                activityType.getUnit(),
                activityType.getDisplayName());
    }
}

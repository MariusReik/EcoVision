package no.ecovision.activity;

/** Thrown when a POST /api/activities request names a code that isn't a known, active activity type. */
public class ActivityTypeNotFoundException extends RuntimeException {

    public ActivityTypeNotFoundException(String activityTypeCode) {
        super("No active activity type with code '%s'".formatted(activityTypeCode));
    }
}

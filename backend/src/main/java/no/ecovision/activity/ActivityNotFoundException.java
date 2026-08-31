package no.ecovision.activity;

import java.util.UUID;

/**
 * Thrown when an activity id does not exist, or exists but belongs to another user.
 * The two cases are deliberately indistinguishable to the caller - a user must not be
 * able to probe for the existence of activities they don't own.
 */
public class ActivityNotFoundException extends RuntimeException {

    public ActivityNotFoundException(UUID id) {
        super("No activity with id '%s'".formatted(id));
    }
}

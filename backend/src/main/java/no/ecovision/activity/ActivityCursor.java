package no.ecovision.activity;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

/**
 * Keyset cursor over (occurred_on DESC, id DESC), matching activity_log_user_date_idx.
 * Opaque to the client: encodes the last row of the previous page so the next page can
 * resume with a WHERE clause instead of an OFFSET, which would skip or repeat rows on a
 * table users append to daily.
 */
record ActivityCursor(LocalDate occurredOn, UUID id) {

    String encode() {
        String raw = occurredOn + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static ActivityCursor decode(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 2);
            return new ActivityCursor(LocalDate.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (RuntimeException e) {
            throw new InvalidCursorException(cursor);
        }
    }
}

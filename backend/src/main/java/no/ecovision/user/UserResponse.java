package no.ecovision.user;

import java.time.OffsetDateTime;
import java.util.UUID;

import no.ecovision.emission.AccountingBasis;

/** The `{user}` object returned by every auth and profile endpoint. Never includes the password hash. */
public record UserResponse(
        UUID id,
        String email,
        String displayName,
        String region,
        AccountingBasis accountingBasis,
        OffsetDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRegion(),
                user.getAccountingBasis(),
                user.getCreatedAt());
    }
}

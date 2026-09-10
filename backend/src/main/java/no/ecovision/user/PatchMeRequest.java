package no.ecovision.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import no.ecovision.emission.AccountingBasis;

/**
 * PATCH /api/me: every field optional, only non-null fields are applied. See
 * ARCHITECTURE.md section 7.
 */
public record PatchMeRequest(

        @Size(max = 100)
        String displayName,

        // ISO 3166-1 alpha-2, or the GLOBAL fallback region. See ARCHITECTURE.md section 6.
        @Pattern(regexp = "^[A-Z]{2}$|^GLOBAL$", message = "must be an ISO 3166-1 alpha-2 code or GLOBAL")
        String region,

        AccountingBasis accountingBasis) {
}

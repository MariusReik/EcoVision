package no.ecovision.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank @Email @Size(max = 320)
        String email,

        // BCrypt silently ignores input past 72 bytes; cap here so that isn't a surprise.
        @NotBlank @Size(min = 8, max = 72)
        String password,

        @NotBlank @Size(max = 100)
        String displayName,

        // ISO 3166-1 alpha-2, or the GLOBAL fallback region. See ARCHITECTURE.md section 6.
        @NotBlank @Pattern(regexp = "^[A-Z]{2}$|^GLOBAL$", message = "must be an ISO 3166-1 alpha-2 code or GLOBAL")
        String region) {
}

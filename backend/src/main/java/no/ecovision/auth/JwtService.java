package no.ecovision.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Issues bearer tokens for a user identity. Validation of tokens on incoming requests is
 * handled separately by the JwtDecoder bean wired into Spring Security's OAuth2 resource
 * server filter (config/JwtConfig.java) - this class only ever writes tokens, never reads them.
 */
@Service
public class JwtService {

    // Spring's Jwt.getIssuer() parses this claim as a URL, so a bare string like
    // "ecovision" fails to decode even though it's a valid RFC 7519 StringOrURI.
    private static final String ISSUER = "https://ecovision.no";

    private final JwtEncoder jwtEncoder;
    private final Duration expiration;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${ecovision.jwt.expiration-minutes}") long expirationMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    /** The subject is the user's id; ownership checks downstream parse it back out of the token. */
    public String issueToken(UUID userId, String email) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .subject(userId.toString())
                .claim("email", email)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

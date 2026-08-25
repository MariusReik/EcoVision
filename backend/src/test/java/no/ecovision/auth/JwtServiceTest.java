package no.ecovision.auth;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test: constructs its own encoder/decoder pair over an in-memory HMAC key
 * rather than pulling in a Spring context, matching how EmissionCalculationServiceTest
 * treats phase 1 as pure domain code where it can be.
 */
class JwtServiceTest {

    private static final String SECRET = "unit-test-signing-key-at-least-32-bytes-long!!";

    private JwtDecoder jwtDecoder;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        var key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(key));
        jwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        jwtService = new JwtService(encoder, 60);
    }

    @Test
    void issueToken_isDecodableAndCarriesUserIdAsSubject() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.issueToken(userId, "person@example.com");
        Jwt decoded = jwtDecoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo(userId.toString());
        assertThat(decoded.getClaimAsString("email")).isEqualTo("person@example.com");
        assertThat(decoded.getIssuer().toString()).contains("ecovision");
    }

    @Test
    void issueToken_expiresSixtyMinutesAfterIssuance() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.issueToken(userId, "person@example.com");
        Jwt decoded = jwtDecoder.decode(token);

        Duration lifetime = Duration.between(decoded.getIssuedAt(), decoded.getExpiresAt());
        assertThat(lifetime).isCloseTo(Duration.ofMinutes(60), Duration.ofSeconds(5));
        assertThat(decoded.getExpiresAt()).isAfter(Instant.now());
    }
}

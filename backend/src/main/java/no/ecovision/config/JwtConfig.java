package no.ecovision.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Symmetric (HS256) signing key for self-issued JWTs. `ecovision.jwt.secret` has no
 * default in application.yml on purpose - see CLAUDE.md and ARCHITECTURE.md section 9
 * ("Config committed with placeholder credentials" was a v1 mistake). Set it via the
 * JWT_SECRET environment variable; the app refuses to start without one.
 */
@Configuration
public class JwtConfig {

    private static final int MIN_SECRET_BYTES = 32; // 256 bits, the HS256 floor per RFC 7518.

    @Bean
    public JwtEncoder jwtEncoder(@Value("${ecovision.jwt.secret}") String secret) {
        ImmutableSecret<SecurityContext> jwkSource = new ImmutableSecret<>(signingKey(secret));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${ecovision.jwt.secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(signingKey(secret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private SecretKey signingKey(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "ecovision.jwt.secret (JWT_SECRET) must be at least %d bytes; got %d. Generate one with, "
                            + "e.g., `openssl rand -base64 32`.".formatted(MIN_SECRET_BYTES, bytes.length));
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}

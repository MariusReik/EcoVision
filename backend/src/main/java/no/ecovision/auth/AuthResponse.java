package no.ecovision.auth;

import no.ecovision.user.UserResponse;

public record AuthResponse(String token, UserResponse user) {
}

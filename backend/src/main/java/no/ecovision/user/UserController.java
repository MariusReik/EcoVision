package no.ecovision.user;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        return userService.getMe(UUID.fromString(jwt.getSubject()));
    }

    @PatchMapping
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PatchMeRequest request) {
        return userService.updateMe(UUID.fromString(jwt.getSubject()), request);
    }
}

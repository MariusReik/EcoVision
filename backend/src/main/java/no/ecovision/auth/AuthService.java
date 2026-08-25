package no.ecovision.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import no.ecovision.user.User;
import no.ecovision.user.UserRepository;
import no.ecovision.user.UserResponse;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(String email, String rawPassword, String displayName, String region) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        User user = new User(email, passwordEncoder.encode(rawPassword), displayName, region);
        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            // Race: two concurrent registrations for the same email both passed the
            // existsBy check above. The DB's case-insensitive unique index is the real
            // guard (users_email_lower_key); translate its violation the same way.
            throw new EmailAlreadyRegisteredException(email);
        }

        return new AuthResponse(jwtService.issueToken(user.getId(), user.getEmail()), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(jwtService.issueToken(user.getId(), user.getEmail()), UserResponse.from(user));
    }
}

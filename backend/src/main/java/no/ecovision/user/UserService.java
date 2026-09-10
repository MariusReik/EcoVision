package no.ecovision.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        return UserResponse.from(findUser(userId));
    }

    @Transactional
    public UserResponse updateMe(UUID userId, PatchMeRequest request) {
        User user = findUser(userId);

        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.region() != null) {
            user.setRegion(request.region());
        }
        if (request.accountingBasis() != null) {
            user.setAccountingBasis(request.accountingBasis());
        }

        return UserResponse.from(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + userId));
    }
}

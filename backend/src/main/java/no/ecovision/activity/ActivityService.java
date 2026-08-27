package no.ecovision.activity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import no.ecovision.emission.ActivityTypeRepository;
import no.ecovision.emission.EmissionCalculationResult;
import no.ecovision.emission.EmissionCalculationService;
import no.ecovision.user.User;
import no.ecovision.user.UserRepository;

/**
 * Creates an activity log entry. The emissions value is computed exactly once here, at
 * write time, and frozen onto the row - see ARCHITECTURE.md section 5, decision 1.
 */
@Service
public class ActivityService {

    private final ActivityTypeRepository activityTypeRepository;
    private final UserRepository userRepository;
    private final EmissionCalculationService emissionCalculationService;
    private final ActivityLogRepository activityLogRepository;

    public ActivityService(
            ActivityTypeRepository activityTypeRepository,
            UserRepository userRepository,
            EmissionCalculationService emissionCalculationService,
            ActivityLogRepository activityLogRepository) {
        this.activityTypeRepository = activityTypeRepository;
        this.userRepository = userRepository;
        this.emissionCalculationService = emissionCalculationService;
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public ActivityResponse create(UUID userId, CreateActivityRequest request) {
        activityTypeRepository.findByCodeAndActiveTrue(request.activityTypeCode())
                .orElseThrow(() -> new ActivityTypeNotFoundException(request.activityTypeCode()));

        // Loaded fresh rather than trusted from the token: region/accounting basis can
        // change after login (PATCH /api/me) and the token isn't reissued when they do.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + userId));

        EmissionCalculationResult result = emissionCalculationService.calculate(
                request.activityTypeCode(),
                user.getRegion(),
                user.getAccountingBasis(),
                request.occurredOn(),
                request.quantity());

        ActivityLog activity = new ActivityLog(
                userId,
                request.activityTypeCode(),
                request.quantity(),
                request.occurredOn(),
                result.emissionFactorId(),
                result.emissionsKg(),
                request.note());

        return ActivityResponse.from(activityLogRepository.save(activity));
    }
}

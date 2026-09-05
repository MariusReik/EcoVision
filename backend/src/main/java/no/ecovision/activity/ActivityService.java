package no.ecovision.activity;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
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

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    // Sentinels standing in for "no bound"/"no cursor yet" so findPage never binds a null
    // (see ActivityLogRepository.findPage). occurred_on can never reach either date in
    // practice: the table forbids future dates, and no activity predates year 1.
    private static final LocalDate EARLIEST_DATE = LocalDate.of(1, 1, 1);
    private static final LocalDate LATEST_DATE = LocalDate.of(9999, 12, 31);
    private static final UUID NIL_CURSOR_ID = new UUID(0L, 0L);

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

    @Transactional
    public void delete(UUID userId, UUID activityId) {
        if (activityLogRepository.deleteByIdAndUserId(activityId, userId) == 0) {
            throw new ActivityNotFoundException(activityId);
        }
    }

    @Transactional(readOnly = true)
    public ActivityPageResponse list(UUID userId, LocalDate from, LocalDate to, String cursor, Integer limit) {
        int pageSize = (limit == null) ? DEFAULT_PAGE_SIZE : Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);

        LocalDate cursorOccurredOn = LATEST_DATE;
        UUID cursorId = NIL_CURSOR_ID;
        if (cursor != null && !cursor.isBlank()) {
            ActivityCursor decoded = ActivityCursor.decode(cursor);
            cursorOccurredOn = decoded.occurredOn();
            cursorId = decoded.id();
        }

        // Fetch one extra row purely to tell whether another page exists, without it
        // ever reaching the response.
        List<ActivityLog> rows = activityLogRepository.findPage(
                userId,
                from == null ? EARLIEST_DATE : from,
                to == null ? LATEST_DATE : to,
                cursorOccurredOn,
                cursorId,
                PageRequest.of(0, pageSize + 1));

        boolean hasMore = rows.size() > pageSize;
        List<ActivityLog> page = hasMore ? rows.subList(0, pageSize) : rows;

        String nextCursor = hasMore
                ? new ActivityCursor(page.get(page.size() - 1).getOccurredOn(), page.get(page.size() - 1).getId()).encode()
                : null;

        return new ActivityPageResponse(page.stream().map(ActivityResponse::from).toList(), nextCursor);
    }
}

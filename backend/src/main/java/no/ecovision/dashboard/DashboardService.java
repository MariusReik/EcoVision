package no.ecovision.dashboard;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Assembles the dashboard summary from three SQL aggregates (ARCHITECTURE.md phase 4).
 * Holds no arithmetic of its own: the database does the summing.
 */
@Service
public class DashboardService {

    // Sentinels standing in for an open-ended window, so the repository never binds a
    // null date (see DashboardRepository). occurred_on can never reach either bound:
    // activity_log forbids future dates and nothing predates year 1.
    private static final LocalDate EARLIEST_DATE = LocalDate.of(1, 1, 1);
    private static final LocalDate LATEST_DATE = LocalDate.of(9999, 12, 31);

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(UUID userId, LocalDate from, LocalDate to) {
        LocalDate effectiveFrom = (from == null) ? EARLIEST_DATE : from;
        LocalDate effectiveTo = (to == null) ? LATEST_DATE : to;

        return new DashboardSummaryResponse(
                dashboardRepository.totalEmissions(userId, effectiveFrom, effectiveTo),
                dashboardRepository.emissionsByCategory(userId, effectiveFrom, effectiveTo),
                dashboardRepository.dailyEmissions(userId, effectiveFrom, effectiveTo));
    }
}

package no.ecovision.dashboard;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import no.ecovision.activity.ActivityLog;

/**
 * Dashboard aggregation (ARCHITECTURE.md phase 4). Every total is computed by the
 * database with GROUP BY / SUM - rows are never pulled into Java and summed in a loop.
 *
 * <p>Ownership is enforced in every query with {@code a.userId = :userId}, not by a
 * post-fetch check (CLAUDE.md). Callers resolve {@code from}/{@code to} to real bounds
 * before calling: a bind parameter that only ever appears in an "IS NULL" test gives
 * Postgres nothing to infer its type from and the query is rejected outright (the same
 * reason ActivityLogRepository.findPage takes sentinel bounds).
 *
 * <p>Extends the {@code Repository} marker rather than {@code JpaRepository} so this
 * read-only aggregation surface exposes nothing but the three queries below.
 */
public interface DashboardRepository extends Repository<ActivityLog, UUID> {

    @Query("""
            SELECT COALESCE(SUM(a.emissionsKg), 0)
            FROM ActivityLog a
            WHERE a.userId = :userId
              AND a.occurredOn >= :from
              AND a.occurredOn <= :to
            """)
    BigDecimal totalEmissions(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT new no.ecovision.dashboard.CategoryEmission(t.category, SUM(a.emissionsKg))
            FROM ActivityLog a
            JOIN ActivityType t ON t.code = a.activityTypeCode
            WHERE a.userId = :userId
              AND a.occurredOn >= :from
              AND a.occurredOn <= :to
            GROUP BY t.category
            ORDER BY t.category
            """)
    List<CategoryEmission> emissionsByCategory(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT new no.ecovision.dashboard.DailyEmission(a.occurredOn, SUM(a.emissionsKg))
            FROM ActivityLog a
            WHERE a.userId = :userId
              AND a.occurredOn >= :from
              AND a.occurredOn <= :to
            GROUP BY a.occurredOn
            ORDER BY a.occurredOn
            """)
    List<DailyEmission> dailyEmissions(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}

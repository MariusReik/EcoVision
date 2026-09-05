package no.ecovision.activity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    /**
     * Ownership is enforced in the query, not by a post-fetch check (CLAUDE.md). Returns
     * the number of rows removed: 0 means the id was absent or owned by someone else.
     */
    int deleteByIdAndUserId(UUID id, UUID userId);

    /**
     * Keyset page ordered (occurred_on DESC, id DESC), matching activity_log_user_date_idx.
     * The pageable's size is the only part of it used - the query itself already
     * expresses the position via cursorOccurredOn/cursorId, so offset must stay 0.
     *
     * Callers pass sentinel, never-null bounds (see ActivityService) rather than nullable
     * from/to/cursor values: a bind parameter used only in an "IS NULL" comparison gives
     * Postgres nothing to infer its type from, and it rejects the query outright.
     */
    @Query("""
            SELECT a FROM ActivityLog a
            WHERE a.userId = :userId
              AND a.occurredOn >= :from
              AND a.occurredOn <= :to
              AND (a.occurredOn < :cursorOccurredOn
                   OR (a.occurredOn = :cursorOccurredOn AND a.id < :cursorId))
            ORDER BY a.occurredOn DESC, a.id DESC
            """)
    List<ActivityLog> findPage(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("cursorOccurredOn") LocalDate cursorOccurredOn,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);
}

package no.ecovision.activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    /**
     * Ownership is enforced in the query, not by a post-fetch check (CLAUDE.md). Returns
     * the number of rows removed: 0 means the id was absent or owned by someone else.
     */
    int deleteByIdAndUserId(UUID id, UUID userId);
}

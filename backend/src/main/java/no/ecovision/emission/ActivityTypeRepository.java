package no.ecovision.emission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityTypeRepository extends JpaRepository<ActivityType, String> {

    Optional<ActivityType> findByCodeAndActiveTrue(String code);

    List<ActivityType> findByActiveTrueOrderBySortOrderAscCodeAsc();
}

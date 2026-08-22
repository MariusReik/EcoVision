package no.ecovision.emission;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Factor resolution per ARCHITECTURE.md section 5, decision 2:
 * 1. Exact region match, valid on the date, wins.
 * 2. Fall back to the GLOBAL region factor valid on the date.
 * 3. If neither exists, the caller must reject the write (never substitute a guess).
 */
public interface EmissionFactorRepository extends JpaRepository<EmissionFactor, Long> {

    @Query("""
            select f from EmissionFactor f
            where f.activityType.code = :typeCode
              and f.region in (:region, 'GLOBAL')
              and (f.accountingBasis is null or f.accountingBasis = :basis)
              and f.validFrom <= :date
              and (f.validTo is null or f.validTo > :date)
            order by case when f.region = 'GLOBAL' then 1 else 0 end, f.validFrom desc
            """)
    List<EmissionFactor> findResolutionCandidates(
            @Param("typeCode") String typeCode,
            @Param("region") String region,
            @Param("basis") AccountingBasis basis,
            @Param("date") LocalDate date,
            Pageable pageable);

    default Optional<EmissionFactor> resolve(String typeCode, String region, AccountingBasis basis, LocalDate date) {
        return findResolutionCandidates(typeCode, region, basis, date, Pageable.ofSize(1))
                .stream()
                .findFirst();
    }
}

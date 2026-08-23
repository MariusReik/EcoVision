package no.ecovision.emission;

import java.time.LocalDate;

/**
 * Thrown when factor resolution (region match or GLOBAL fallback) finds nothing
 * valid on the given date. Never substitute a guess; the caller must reject the write.
 */
public class FactorNotFoundException extends RuntimeException {

    private final String typeCode;
    private final String region;
    private final AccountingBasis basis;
    private final LocalDate date;

    public FactorNotFoundException(String typeCode, String region, AccountingBasis basis, LocalDate date) {
        super("No emission factor found for type '%s', region '%s', basis '%s', date %s (region and GLOBAL both checked)"
                .formatted(typeCode, region, basis, date));
        this.typeCode = typeCode;
        this.region = region;
        this.basis = basis;
        this.date = date;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getRegion() {
        return region;
    }

    public AccountingBasis getBasis() {
        return basis;
    }

    public LocalDate getDate() {
        return date;
    }
}

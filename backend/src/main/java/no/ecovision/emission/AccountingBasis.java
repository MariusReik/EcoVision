package no.ecovision.emission;

/**
 * 'LOCATION' uses the physical grid mix; 'MARKET' uses the residual mix after
 * guarantees of origin are sold. See ARCHITECTURE.md section 6.
 */
public enum AccountingBasis {
    LOCATION,
    MARKET
}

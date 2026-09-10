package no.ecovision.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

import no.ecovision.emission.AccountingBasis;

/**
 * A registered account. Matches V1__baseline.sql exactly; if they disagree,
 * fix this entity, not the migration.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    // ISO 3166-1 alpha-2, or 'GLOBAL'. See ARCHITECTURE.md section 6.
    @Column(name = "region", nullable = false, length = 10)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(name = "accounting_basis", nullable = false, length = 10)
    private AccountingBasis accountingBasis;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected User() {
        // JPA
    }

    public User(String email, String passwordHash, String displayName, String region) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.region = region;
        this.accountingBasis = AccountingBasis.LOCATION;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRegion() {
        return region;
    }

    public AccountingBasis getAccountingBasis() {
        return accountingBasis;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setAccountingBasis(AccountingBasis accountingBasis) {
        this.accountingBasis = accountingBasis;
    }
}

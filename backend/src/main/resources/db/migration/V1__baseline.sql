-- EcoVision v2 baseline schema
-- Postgres 16. Applied by Flyway on application startup.

CREATE TABLE users (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email             VARCHAR(320) NOT NULL,
    password_hash     VARCHAR(255) NOT NULL,
    display_name      VARCHAR(100) NOT NULL,

    -- ISO 3166-1 alpha-2, or 'GLOBAL' for the fallback factor set.
    region            VARCHAR(10)  NOT NULL DEFAULT 'GLOBAL',

    -- 'LOCATION' uses the physical grid mix; 'MARKET' uses the residual mix
    -- after guarantees of origin are sold. See ARCHITECTURE.md section 6.
    accounting_basis  VARCHAR(10)  NOT NULL DEFAULT 'LOCATION',

    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT users_accounting_basis_check
        CHECK (accounting_basis IN ('LOCATION', 'MARKET'))
);

-- Case-insensitive uniqueness. A plain UNIQUE(email) would let the same person
-- register as marius@x.no and Marius@X.no.
CREATE UNIQUE INDEX users_email_lower_key ON users (lower(email));


-- Reference table. What a user can log, and in what unit.
CREATE TABLE activity_type (
    code          VARCHAR(50)  PRIMARY KEY,
    category      VARCHAR(30)  NOT NULL,
    display_name  VARCHAR(100) NOT NULL,

    -- The unit `quantity` is expressed in: 'km', 'kWh', 'kg', 'litre', 'meal'.
    unit          VARCHAR(20)  NOT NULL,

    sort_order    INT          NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT activity_type_category_check
        CHECK (category IN ('TRANSPORT', 'ENERGY', 'FOOD', 'WASTE'))
);


-- Versioned, region-scoped conversion factors.
-- A row is valid over [valid_from, valid_to), with valid_to NULL meaning "current".
CREATE TABLE emission_factor (
    id                    BIGSERIAL PRIMARY KEY,
    activity_type_code    VARCHAR(50) NOT NULL REFERENCES activity_type(code),

    region                VARCHAR(10) NOT NULL DEFAULT 'GLOBAL',
    accounting_basis      VARCHAR(10),  -- NULL = applies to both bases

    -- kg CO2e per one unit of the activity type. Six decimals because grid
    -- factors are on the order of 0.017 and rounding matters at that scale.
    factor_kg_co2e        NUMERIC(12, 6) NOT NULL,

    -- Provenance. Never seed a factor without filling these in.
    source                TEXT        NOT NULL,
    source_year           INT         NOT NULL,
    source_url            TEXT,

    valid_from            DATE        NOT NULL,
    valid_to              DATE,

    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT emission_factor_positive
        CHECK (factor_kg_co2e >= 0),
    CONSTRAINT emission_factor_valid_range
        CHECK (valid_to IS NULL OR valid_to > valid_from),
    CONSTRAINT emission_factor_basis_check
        CHECK (accounting_basis IS NULL OR accounting_basis IN ('LOCATION', 'MARKET'))
);

-- Resolution always filters on these three columns together.
CREATE INDEX emission_factor_lookup_idx
    ON emission_factor (activity_type_code, region, valid_from DESC);

-- Prevent two overlapping factors for the same type/region/basis, which would make
-- resolution non-deterministic. Requires the btree_gist extension.
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE emission_factor
    ADD CONSTRAINT emission_factor_no_overlap
    EXCLUDE USING gist (
        activity_type_code WITH =,
        region WITH =,
        COALESCE(accounting_basis, '*') WITH =,
        daterange(valid_from, valid_to, '[)') WITH &&
    );


CREATE TABLE activity_log (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_type_code  VARCHAR(50) NOT NULL REFERENCES activity_type(code),

    quantity            NUMERIC(12, 3) NOT NULL,
    occurred_on         DATE NOT NULL,

    -- Both of these are written once, at insert time, and never recomputed.
    -- See ARCHITECTURE.md section 5, decision 1.
    emission_factor_id  BIGINT NOT NULL REFERENCES emission_factor(id),
    emissions_kg        NUMERIC(14, 4) NOT NULL,

    note                VARCHAR(280),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT activity_log_quantity_positive CHECK (quantity > 0),
    CONSTRAINT activity_log_not_future        CHECK (occurred_on <= CURRENT_DATE)
);

-- Serves both the paginated activity list and the dashboard date-range aggregation.
CREATE INDEX activity_log_user_date_idx
    ON activity_log (user_id, occurred_on DESC, id DESC);
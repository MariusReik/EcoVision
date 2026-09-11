-- EcoVision v2: reference data seed
-- Postgres 16. Applied by Flyway on application startup, after V1 (schema) and
-- V2 (currently empty -- a known gap tracked separately; not this migration's job).
--
-- Populates `activity_type` and `emission_factor` per ARCHITECTURE.md section 6.
-- Every factor below was looked up against a real published source while writing
-- this migration (not recalled from memory) and is cited with source, source_year
-- and source_url so a reviewer can go and check the number themselves. Where a
-- figure could not be pinned to an authoritative source, no row was inserted --
-- see the TODO(verify) near the bottom instead of a guess.
--
-- Three source families, per ARCHITECTURE.md section 6:
--   1. DESNZ (formerly DEFRA/BEIS) "UK Government GHG Conversion Factors for
--      Company Reporting", 2024 edition, condensed set v1.1. Figures below were
--      read directly out of that spreadsheet's worksheets (Passenger vehicles,
--      Business travel- land, Business travel- air, Fuels, UK electricity,
--      Waste disposal), not a secondary summary, to avoid transcription drift.
--      https://www.gov.uk/government/publications/greenhouse-gas-reporting-conversion-factors-2024
--   2. Poore & Nemecek (2018), Science 360(6392):987-992, "Reducing food's
--      environmental impacts through producers and consumers" -- global median
--      kg CO2e per kg product across ~38,700 farms, as summarised by Our World
--      in Data. https://ourworldindata.org/environmental-impacts-of-food
--   3. NVE (Norges vassdrags- og energidirektorat) for Norwegian electricity.
--
-- All factors are "current" (valid_to NULL); there is exactly one row per
-- (activity_type_code, region, accounting_basis) triple below, so the
-- emission_factor_no_overlap exclusion constraint from V1 is satisfied trivially.


-- ---------------------------------------------------------------------------
-- activity_type
-- ---------------------------------------------------------------------------
-- Note on TRANSPORT units: the DESNZ bus/rail/flight factors are per
-- *passenger*.km, not per vehicle.km (see comments on the emission_factor rows
-- below). For a personal footprint tracker this distinction doesn't need a
-- separate unit: the distance a user logs for a bus or train trip already is
-- their own passenger.km, so 'km' as the logging unit is correct as-is.

INSERT INTO activity_type (code, category, display_name, unit, sort_order, active) VALUES
    ('car_petrol',       'TRANSPORT', 'Car (petrol, average)',        'km',   10, TRUE),
    ('car_diesel',       'TRANSPORT', 'Car (diesel, average)',        'km',   20, TRUE),
    ('bus',              'TRANSPORT', 'Bus',                          'km',   30, TRUE),
    ('train',            'TRANSPORT', 'Train (national rail)',       'km',   40, TRUE),
    ('flight_short_haul','TRANSPORT', 'Flight (short-haul)',          'km',   50, TRUE),

    ('electricity',      'ENERGY',    'Electricity',                  'kWh',  60, TRUE),
    ('natural_gas',      'ENERGY',    'Natural gas',                  'kWh',  70, TRUE),

    ('beef',             'FOOD',      'Beef',                        'kg',   80, TRUE),
    ('chicken',          'FOOD',      'Chicken',                     'kg',   90, TRUE),
    ('pork',             'FOOD',      'Pork',                        'kg',  100, TRUE),
    ('milk',             'FOOD',      'Milk',                        'litre',110, TRUE),
    ('rice',             'FOOD',      'Rice',                        'kg',  120, TRUE),

    ('waste_landfill',   'WASTE',     'General waste (to landfill)', 'kg',  130, TRUE);


-- ---------------------------------------------------------------------------
-- emission_factor: TRANSPORT
-- ---------------------------------------------------------------------------
-- Source workbook: DESNZ "Greenhouse gas reporting: conversion factors 2024"
-- (condensed set v1.1, factor year 2024). Car figures are identical in both
-- the "Passenger vehicles" (Scope 1, owned vehicles) and "Business travel-
-- land" (Scope 3, non-owned) tabs -- the guidance sheet says so explicitly --
-- so it doesn't matter which tab we attribute them to; cited by tab below.
-- All car/bus/rail/flight figures in the workbook are already expressed in
-- kg CO2e per km (cars) or per passenger.km (bus/rail/flight); no unit
-- conversion was necessary beyond reading the correct column.

INSERT INTO emission_factor
    (activity_type_code, region, accounting_basis, factor_kg_co2e, source, source_year, source_url, valid_from, valid_to)
VALUES
    -- "Passenger vehicles" tab, "Cars (by size)" table, row "Average car",
    -- column "Petrol" -> kg CO2e (total, incl. CH4/N2O), unit km.
    ('car_petrol', 'GLOBAL', NULL, 0.164500,
     'DESNZ UK Government GHG Conversion Factors for Company Reporting 2024, "Passenger vehicles" tab (Cars by size: Average car, Petrol, km)',
     2024, 'https://www.gov.uk/government/publications/greenhouse-gas-reporting-conversion-factors-2024',
     '2024-01-01', NULL),

    -- Same table, row "Average car", column "Diesel", unit km.
    ('car_diesel', 'GLOBAL', NULL, 0.169840,
     'DESNZ UK Government GHG Conversion Factors for Company Reporting 2024, "Passenger vehicles" tab (Cars by size: Average car, Diesel, km)',
     2024, 'https://www.gov.uk/government/publications/greenhouse-gas-reporting-conversion-factors-2024',
     '2024-01-01', NULL),

    -- "Business travel- land" tab, "Bus" table, row "Average local bus",
    -- unit passenger.km. (Distinct from "Local London bus" and "Coach", which
    -- have their own, lower/different factors -- "average local bus" is the
    -- representative pick for a generic bus activity type.)
    ('bus', 'GLOBAL', NULL, 0.108460,
     'DESNZ UK Government GHG Conversion Factors for Company Reporting 2024, "Business travel- land" tab (Bus: Average local bus, passenger.km)',
     2024, 'https://www.gov.uk/government/publications/greenhouse-gas-reporting-conversion-factors-2024',
     '2024-01-01', NULL),

    -- "Business travel- land" tab, "Rail" table, row "National rail",
    -- unit passenger.km.
    ('train', 'GLOBAL', NULL, 0.035460,
     'DESNZ UK Government GHG Conversion Factors for Company Reporting 2024, "Business travel- land" tab (Rail: National rail, passenger.km)',
     2024, 'https://www.gov.uk/government/publications/greenhouse-gas-reporting-conversion-factors-2024',
     '2024-01-01', NULL),

    -- "Business travel- air" tab, "Flights" table, row "Short-haul, to/from
    -- UK", column "Average passenger", "With RF" (radiative forcing) figure,
    -- unit passenger.km. DESNZ's own guidance recommends using the "with RF"
    -- factor (it applies a 70% uplift to the CO2 figure to approximate the
    -- non-CO2 climate effects of aviation -- contrails, NOx, water vapour at
    -- altitude) for reporting that captures the full climate impact, rather
    -- than the lower "without RF" (direct-effects-only) figure.
    ('flight_short_haul', 'GLOBAL', NULL, 0.185920,
     'DESNZ UK Government GHG Conversion Factors for Company Reporting 2024, "Business travel- air" tab (Flights: Short-haul to/from UK, Average passenger, passenger.km, incl. radiative forcing)',
     2024, 'https://www.gov.uk/government/publications/greenhouse-gas-reporting-conversion-factors-2024',
     '2024-01-01', NULL);


-- ---------------------------------------------------------------------------
-- emission_factor: ENERGY
-- ---------------------------------------------------------------------------

INSERT INTO emission_factor
    (activity_type_code, region, accounting_basis, factor_kg_co2e, source, source_year, source_url, valid_from, valid_to)
VALUES
    -- "Fuels" tab, "Gaseous fuels" table, row "Natural gas", column
    -- "kWh (Gross CV)". Gross CV is the correct basis per the workbook's own
    -- guidance: "the majority of energy billing is provided on a gross CV
    -- basis" -- which is how a household user will read quantity off a gas
    -- bill -- as opposed to net CV, which is used for transport fuel. No
    -- further conversion needed; the figure is already kg CO2e per kWh.
    ('natural_gas', 'GLOBAL', NULL, 0.182900,
     'DESNZ UK Government GHG Conversion Factors for Company Reporting 2024, "Fuels" tab (Gaseous fuels: Natural gas, kWh Gross CV)',
     2024, 'https://www.gov.uk/government/publications/greenhouse-gas-reporting-conversion-factors-2024',
     '2024-01-01', NULL),

    -- GLOBAL fallback for electricity, LOCATION basis: DESNZ "UK electricity"
    -- tab, "Electricity generated" table, row "Electricity: UK", year 2024.
    -- The workbook's own guidance sheet describes this as a "location-based
    -- grid average emissions factor", which is exactly the GLOBAL/LOCATION
    -- row this schema needs as a resolution fallback (ARCHITECTURE.md
    -- section 5, decision 2: exact region -> GLOBAL -> reject). Already
    -- kg CO2e per kWh; no conversion needed.
    ('electricity', 'GLOBAL', 'LOCATION', 0.207050,
     'DESNZ UK Government GHG Conversion Factors for Company Reporting 2024, "UK electricity" tab (Electricity generated: Electricity: UK, kWh, 2024, location-based)',
     2024, 'https://www.gov.uk/government/publications/greenhouse-gas-reporting-conversion-factors-2024',
     '2024-01-01', NULL),

    -- Norway, LOCATION basis (physical production mix / "klimadeklarasjon").
    -- NVE's calculation for calendar year 2024: average climate-gas emissions
    -- related to electricity use in Norway were 11.9 g CO2e/kWh (down from
    -- 15 g CO2e/kWh in 2023), on the back of ~95% renewable generation
    -- (83% hydro, 11% wind, 1% solar). This is well below the ~17-28 g/kWh
    -- range ARCHITECTURE.md quotes as a rough historical estimate -- 2024 was
    -- an unusually wet/windy generation year -- but it is NVE's actual
    -- current published figure for 2024, so it is used as-is rather than the
    -- older approximate range. Conversion: 11.9 g CO2e/kWh = 0.0119 kg CO2e/kWh.
    ('electricity', 'NO', 'LOCATION', 0.011900,
     'NVE (Norges vassdrags- og energidirektorat) klimadeklarasjon for physically delivered electricity, 2024: 11.9 g CO2e/kWh, reported via EnergiAktuelt',
     2024, 'https://www.energiaktuelt.no/stroemmens-klimagassutslipp-ned-fra-15-til-119-g-co-ekvkwh.6723599-575505.html',
     '2024-01-01', NULL);

    -- TODO(verify): Norway MARKET-basis (residual mix) electricity factor per
    -- NVE varedeklarasjon -- see ARCHITECTURE.md section 6.
    --
    -- Attempted to source this directly from NVE (nve.no) during this session
    -- via WebSearch/WebFetch. NVE's own varedeklarasjon pages describe the
    -- methodology (electricity suppliers without purchased guarantees of
    -- origin must use the European residual mix figure that NVE publishes
    -- annually) but the specific current numeric value could not be
    -- confirmed by directly reading an NVE page or NVE-published document in
    -- this session -- NVE's FAQ page only shows a historical 2018 figure
    -- (520 g CO2e/kWh), and a direct fetch of NVE's "Varedeklarasjon for
    -- strømleverandører" landing page returned no numeric figure at all.
    -- Three different secondary sources (electricity retailers / search
    -- summaries) gave three different, mutually inconsistent numbers for
    -- what should be the same figure: ~535 g CO2e/kWh (unattributed, for
    -- "2024"), 431 g CO2e/kWh (attributed to NVE, dated 2025, via
    -- ishavskraft.no), and ~400 g CO2e/kWh ("the last two years", via
    -- fjordkraft.no, no precise year). Given the spread and the inability to
    -- verify any one of them against NVE's own published data, inserting any
    -- of them would risk exactly the "plausible-looking made-up number"
    -- CLAUDE.md warns against. Before seeding this row, download NVE's
    -- "Stromdeklarasjoner Faktorer" spreadsheet directly from
    -- https://www.nve.no/energi/energisystem/energibruk/stroemdeklarasjoner/
    -- (linked from that page as a downloadable factor file) and read the
    -- current varedeklarasjon (residual mix) row for the latest reporting
    -- year, then add it here as a new row with region='NO',
    -- accounting_basis='MARKET'.


-- ---------------------------------------------------------------------------
-- emission_factor: FOOD
-- ---------------------------------------------------------------------------
-- Source: Poore & Nemecek (2018), Science, as summarised by Our World in Data
-- ("Environmental Impacts of Food Production" / "Greenhouse gas emissions per
-- kilogram of food product"), global median kg CO2e per kg product across
-- ~38,700 farms in 119 countries, including land-use change -- which is why
-- these figures (especially beef) read high relative to farm-gate-only
-- estimates. Confirmed directly against an OWID-sourced chart citing
-- "Data source: Poore and Nemecek (2018), OurWorldInData.org/environmental-
-- impacts-of-food" during this session, not recalled from memory.
--
-- "Beef" uses the "Beef (beef herd)" figure (dedicated beef production
-- systems), not the lower "Beef (dairy herd)" figure (beef as a co-product of
-- dairy farming) -- this is the figure most commonly meant by an unqualified
-- "beef" activity and the one most secondary sources quote as "beef".
-- "Chicken" and "Pork" map to OWID's "Poultry Meat" and "Pig Meat" rows.
-- "Milk" is seeded per litre even though OWID's figure is per kg of product;
-- milk's density is ~1.03 kg/L, close enough to 1:1 that no separate
-- conversion factor is warranted at this precision, but this approximation is
-- called out here for auditability.

INSERT INTO emission_factor
    (activity_type_code, region, accounting_basis, factor_kg_co2e, source, source_year, source_url, valid_from, valid_to)
VALUES
    ('beef', 'GLOBAL', NULL, 99.480000,
     'Poore & Nemecek (2018), Science 360(6392):987-992, via Our World in Data "Environmental Impacts of Food Production" (Beef, beef herd; global median kg CO2e/kg, incl. land-use change)',
     2018, 'https://ourworldindata.org/environmental-impacts-of-food',
     '2018-01-01', NULL),

    ('chicken', 'GLOBAL', NULL, 9.870000,
     'Poore & Nemecek (2018), Science 360(6392):987-992, via Our World in Data "Environmental Impacts of Food Production" (Poultry Meat; global median kg CO2e/kg, incl. land-use change)',
     2018, 'https://ourworldindata.org/environmental-impacts-of-food',
     '2018-01-01', NULL),

    ('pork', 'GLOBAL', NULL, 12.310000,
     'Poore & Nemecek (2018), Science 360(6392):987-992, via Our World in Data "Environmental Impacts of Food Production" (Pig Meat; global median kg CO2e/kg, incl. land-use change)',
     2018, 'https://ourworldindata.org/environmental-impacts-of-food',
     '2018-01-01', NULL),

    ('milk', 'GLOBAL', NULL, 3.150000,
     'Poore & Nemecek (2018), Science 360(6392):987-992, via Our World in Data "Environmental Impacts of Food Production" (Milk; global median kg CO2e/kg product, approximated here as per litre)',
     2018, 'https://ourworldindata.org/environmental-impacts-of-food',
     '2018-01-01', NULL),

    ('rice', 'GLOBAL', NULL, 4.450000,
     'Poore & Nemecek (2018), Science 360(6392):987-992, via Our World in Data "Environmental Impacts of Food Production" (Rice; global median kg CO2e/kg, incl. land-use change)',
     2018, 'https://ourworldindata.org/environmental-impacts-of-food',
     '2018-01-01', NULL);


-- ---------------------------------------------------------------------------
-- emission_factor: WASTE
-- ---------------------------------------------------------------------------
-- "Waste disposal" tab, "Refuse" table, row "Household residual waste",
-- column "Landfill", unit tonnes: 497.04416 kg CO2e/tonne. This figure is
-- "gate to grave" per the tab's guidance (collection, transportation and
-- landfill emissions combined). "Household residual waste" was chosen over
-- "Commercial and industrial waste" (520.3342 kg CO2e/tonne) because it is
-- the closer match to the "general/mixed municipal waste" activity type
-- described in the task -- ordinary household bin waste, not commercial.
-- Conversion: activity_type.unit is 'kg', so divide the tonne figure by 1000:
--   497.04416 / 1000 = 0.49704416 kg CO2e/kg, rounded to the column's 6
--   decimal places -> 0.497044.

INSERT INTO emission_factor
    (activity_type_code, region, accounting_basis, factor_kg_co2e, source, source_year, source_url, valid_from, valid_to)
VALUES
    ('waste_landfill', 'GLOBAL', NULL, 0.497044,
     'DESNZ UK Government GHG Conversion Factors for Company Reporting 2024, "Waste disposal" tab (Refuse: Household residual waste, Landfill, 497.04416 kg CO2e/tonne / 1000 = kg CO2e/kg)',
     2024, 'https://www.gov.uk/government/publications/greenhouse-gas-reporting-conversion-factors-2024',
     '2024-01-01', NULL);

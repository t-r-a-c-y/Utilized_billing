-- =============================================================================
--  Utility Billing System - Sample Test Data
--  Run AFTER tables exist. Passwords are BCrypt hashes of the noted raw values.
--  (The application also seeds admin/operator/finance accounts automatically.)
-- =============================================================================

-- ---- Users (raw password shown in comments; hash = BCrypt) -------------------
-- admin@utility.rw / Admin123!  is auto-seeded by DataInitializer.
-- A sample customer login (raw: Customer123!):
INSERT INTO users (full_names, email, phone_number, password, role, status, created_at, updated_at)
VALUES ('John Habimana', 'john@example.rw', '+250788654321',
        '$2a$10$Dow1Q9Qm5oJqФAKE_REPLACE_WITH_REAL_HASH', 'ROLE_CUSTOMER', 'ACTIVE', NOW(), NOW());
-- NOTE: generate a real hash with BCryptPasswordEncoder, or just sign up via the API.

-- ---- Customers ---------------------------------------------------------------
INSERT INTO customers (full_names, national_id, email, phone_number, address, status, created_at, updated_at) VALUES
('John Habimana',   '1199080012345678', 'john@example.rw',  '+250788654321', 'KG 11 Ave, Kigali',   'ACTIVE',   NOW(), NOW()),
('Aline Uwase',     '1198570087654321', 'aline@example.rw', '+250788111222', 'KN 5 Rd, Kigali',     'ACTIVE',   NOW(), NOW()),
('Eric Niyonzima',  '1199170055667788', 'eric@example.rw',  '+250788333444', 'Musanze, North',      'INACTIVE', NOW(), NOW());

-- ---- Meters (customer_id assumes the order above => 1,2,3) -------------------
INSERT INTO meters (meter_number, meter_type, installation_date, status, customer_id, created_at, updated_at) VALUES
('MTR-EL-0001', 'ELECTRICITY', '2025-01-15', 'ACTIVE',   1, NOW(), NOW()),
('MTR-WT-0001', 'WATER',       '2025-02-10', 'ACTIVE',   1, NOW(), NOW()),
('MTR-EL-0002', 'ELECTRICITY', '2025-03-01', 'ACTIVE',   2, NOW(), NOW()),
('MTR-WT-0002', 'WATER',       '2025-03-05', 'INACTIVE', 3, NOW(), NOW());

-- ---- Tariffs -----------------------------------------------------------------
-- Electricity: TIERED, version 1, effective from 2026-01-01
INSERT INTO tariffs (name, meter_type, tariff_type, version, rate_per_unit, service_charge, effective_start, effective_end, created_at, updated_at)
VALUES ('Electricity Residential 2026', 'ELECTRICITY', 'TIERED', 1, NULL, 1500.00, '2026-01-01', NULL, NOW(), NOW());
-- Tiers for the electricity tariff (tariff_id = 1): 0-20 @89, 21-50 @212, 51+ @249
INSERT INTO tariff_tiers (tariff_id, up_to_unit, rate_per_unit) VALUES
(1, 20.00, 89.0000),
(1, 50.00, 212.0000),
(1, NULL,  249.0000);

-- Water: FLAT, version 1, effective from 2026-01-01, 340 FRW per unit
INSERT INTO tariffs (name, meter_type, tariff_type, version, rate_per_unit, service_charge, effective_start, effective_end, created_at, updated_at)
VALUES ('Water Residential 2026', 'WATER', 'FLAT', 1, 340.0000, 1000.00, '2026-01-01', NULL, NOW(), NOW());

-- ---- Tax (VAT 18%) -----------------------------------------------------------
INSERT INTO taxes (name, percentage, version, effective_start, effective_end, created_at, updated_at)
VALUES ('VAT', 18.00, 1, '2026-01-01', NULL, NOW(), NOW());

-- ---- Penalty (5% late) -------------------------------------------------------
INSERT INTO penalties (name, percentage, version, effective_start, effective_end, created_at, updated_at)
VALUES ('Late payment penalty', 5.00, 1, '2026-01-01', NULL, NOW(), NOW());

-- ---- Meter readings (meter_id 1 = electricity, customer 1) -------------------
INSERT INTO meter_readings (meter_id, previous_reading, current_reading, consumption, reading_date, reading_month, reading_year, billed, created_at, updated_at)
VALUES (1, 1200.00, 1320.00, 120.00, '2026-05-31', 5, 2026, FALSE, NOW(), NOW());
-- Water reading for meter_id 2, customer 1
INSERT INTO meter_readings (meter_id, previous_reading, current_reading, consumption, reading_date, reading_month, reading_year, billed, created_at, updated_at)
VALUES (2, 80.00, 95.00, 15.00, '2026-05-31', 5, 2026, FALSE, NOW(), NOW());

-- After loading this, call the API:
--   POST /api/v1/bills/generate { "meterId":1, "month":5, "year":2026 }
-- to exercise the billing calculation against the seeded tariffs.

-- 02_migrate_reference_and_store.sql
--
-- Purpose:
--   Migrate reference data + merchant stores from legacy schema into new schema.
--
-- IMPORTANT:
--   Column names in legacy tables may differ. Adjust SELECT lists accordingly after inspecting legacy DDL.
--
-- Legacy tables referenced (expected, adjust as needed):
--   legacy.COUNTRY, legacy.ZONE, legacy.LANGUAGE, legacy.CURRENCY, legacy.MERCHANT_STORE
--
-- Strategy:
--   - Insert reference rows keyed by their legacy codes (iso/code).
--   - Generate UUIDs in new schema and record mappings in shopizer_migration.map_*.

BEGIN;

-- Countries
INSERT INTO shopizer.country (id, iso_code, name)
SELECT
  gen_random_uuid() AS id,
  c.ISO_CODE,
  c.NAME
FROM legacy.COUNTRY c
ON CONFLICT (iso_code) DO NOTHING;

-- Map countries by iso_code (best-effort; if legacy has numeric PK, use it)
-- Example assumes legacy has COUNTRY_ID and ISO_CODE:
INSERT INTO shopizer_migration.map_country (legacy_id, new_id)
SELECT
  c.COUNTRY_ID,
  sc.id
FROM legacy.COUNTRY c
JOIN shopizer.country sc ON sc.iso_code = c.ISO_CODE
ON CONFLICT (legacy_id) DO NOTHING;

-- Languages
INSERT INTO shopizer.language (id, code, name)
SELECT
  gen_random_uuid(),
  l.CODE,
  l.NAME
FROM legacy.LANGUAGE l
ON CONFLICT (code) DO NOTHING;

INSERT INTO shopizer_migration.map_language (legacy_id, new_id)
SELECT
  l.LANGUAGE_ID,
  sl.id
FROM legacy.LANGUAGE l
JOIN shopizer.language sl ON sl.code = l.CODE
ON CONFLICT (legacy_id) DO NOTHING;

-- Currencies
INSERT INTO shopizer.currency (id, code, name, symbol)
SELECT
  gen_random_uuid(),
  cu.CODE,
  cu.NAME,
  cu.SYMBOL
FROM legacy.CURRENCY cu
ON CONFLICT (code) DO NOTHING;

INSERT INTO shopizer_migration.map_currency (legacy_id, new_id)
SELECT
  cu.CURRENCY_ID,
  sc.id
FROM legacy.CURRENCY cu
JOIN shopizer.currency sc ON sc.code = cu.CODE
ON CONFLICT (legacy_id) DO NOTHING;

-- Zones (states/provinces)
INSERT INTO shopizer.zone (id, country_id, code, name)
SELECT
  gen_random_uuid(),
  mc.new_id AS country_id,
  z.CODE,
  z.NAME
FROM legacy.ZONE z
JOIN shopizer_migration.map_country mc ON mc.legacy_id = z.COUNTRY_ID
ON CONFLICT (country_id, code) DO NOTHING;

INSERT INTO shopizer_migration.map_zone (legacy_id, new_id)
SELECT
  z.ZONE_ID,
  sz.id
FROM legacy.ZONE z
JOIN shopizer_migration.map_country mc ON mc.legacy_id = z.COUNTRY_ID
JOIN shopizer.zone sz ON sz.country_id = mc.new_id AND sz.code = z.CODE
ON CONFLICT (legacy_id) DO NOTHING;

-- Merchant stores
INSERT INTO shopizer.merchant_store (
  id,
  store_code,
  store_name,
  country_id,
  zone_id,
  default_language_id,
  currency_id,
  use_cache,
  is_active
)
SELECT
  gen_random_uuid(),
  ms.STORE_CODE,
  ms.STORE_NAME,
  mc.new_id AS country_id,
  mz.new_id AS zone_id,
  ml.new_id AS default_language_id,
  mcu.new_id AS currency_id,
  COALESCE(ms.USE_CACHE, false),
  COALESCE(ms.IS_ACTIVE, true)
FROM legacy.MERCHANT_STORE ms
JOIN shopizer_migration.map_country mc ON mc.legacy_id = ms.COUNTRY_ID
LEFT JOIN shopizer_migration.map_zone mz ON mz.legacy_id = ms.ZONE_ID
JOIN shopizer_migration.map_language ml ON ml.legacy_id = ms.DEFAULT_LANGUAGE_ID
JOIN shopizer_migration.map_currency mcu ON mcu.legacy_id = ms.CURRENCY_ID
ON CONFLICT (store_code) DO NOTHING;

INSERT INTO shopizer_migration.map_merchant_store (legacy_id, new_id)
SELECT
  ms.MERCHANT_ID,
  sms.id
FROM legacy.MERCHANT_STORE ms
JOIN shopizer.merchant_store sms ON sms.store_code = ms.STORE_CODE
ON CONFLICT (legacy_id) DO NOTHING;

COMMIT;

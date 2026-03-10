-- 04_migrate_customers.sql
--
-- Purpose:
--   Migrate customers into new schema.
--
-- Legacy tables referenced (expected, adjust as needed):
--   legacy.CUSTOMER
--
-- Notes:
--   Legacy embeds billing/delivery fields directly in CUSTOMER; this script packs them into JSONB.
--   Adjust field names to match the legacy DDL export.

BEGIN;

INSERT INTO shopizer.customer (
  id,
  merchant_store_id,
  email,
  first_name,
  last_name,
  default_language_id,
  billing_address,
  delivery_address,
  is_active
)
SELECT
  gen_random_uuid(),
  mms.new_id,
  c.EMAIL,
  c.FIRST_NAME,
  c.LAST_NAME,
  ml.new_id,
  jsonb_build_object(
    'address', c.BILLING_ADDRESS,
    'city', c.BILLING_CITY,
    'postalCode', c.BILLING_POSTAL_CODE,
    'country', c.BILLING_COUNTRY,
    'state', c.BILLING_STATE,
    'phone', c.BILLING_PHONE
  ),
  jsonb_build_object(
    'address', c.DELIVERY_ADDRESS,
    'city', c.DELIVERY_CITY,
    'postalCode', c.DELIVERY_POSTAL_CODE,
    'country', c.DELIVERY_COUNTRY,
    'state', c.DELIVERY_STATE,
    'phone', c.DELIVERY_PHONE
  ),
  COALESCE(c.ACTIVE, true)
FROM legacy.CUSTOMER c
JOIN shopizer_migration.map_merchant_store mms ON mms.legacy_id = c.MERCHANT_ID
JOIN shopizer_migration.map_language ml ON ml.legacy_id = c.DEFAULT_LANGUAGE_ID
ON CONFLICT (merchant_store_id, email) DO NOTHING;

INSERT INTO shopizer_migration.map_customer (legacy_id, new_id)
SELECT
  c.CUSTOMER_ID,
  sc.id
FROM legacy.CUSTOMER c
JOIN shopizer_migration.map_merchant_store mms ON mms.legacy_id = c.MERCHANT_ID
JOIN shopizer.customer sc ON sc.merchant_store_id = mms.new_id AND sc.email = c.EMAIL
ON CONFLICT (legacy_id) DO NOTHING;

COMMIT;

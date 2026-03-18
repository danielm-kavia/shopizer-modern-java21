-- Flyway Migration: V2__catalog_seed_mvp
-- Service: catalog-service
-- Purpose:
--   Minimal, idempotent seed data to support an end-to-end MVP happy path:
--   browse catalog -> add to cart -> checkout -> create order.
--
-- Assumptions:
-- - Shared schema "shopizer" is used (configured in application.yml).
-- - If rows already exist, we do not overwrite them.

BEGIN;

-- Deterministic IDs make docs/examples reproducible.
-- Store / language IDs are reused across services (where UUID columns are used).
DO $$
BEGIN
  -- A single MVP store (only if the shared store table exists in this environment).
  -- Some environments may run only service-local migrations; in that case, merchant_store isn't present.
  IF to_regclass('shopizer.merchant_store') IS NOT NULL THEN
    INSERT INTO shopizer.merchant_store (id, store_code, store_name, created_at, updated_at)
    VALUES (
      '00000000-0000-0000-0000-000000000001'::uuid,
      'DEFAULT',
      'Default Store',
      now(),
      now()
    )
    ON CONFLICT (id) DO NOTHING;
  END IF;

  -- Products reference the deterministic MVP store UUID regardless of whether merchant_store is present,
  -- because product.merchant_store_id is not enforced via FK in this service-local schema.
  -- Product 1
  INSERT INTO shopizer.product (id, merchant_store_id, sku, type, is_available, created_at, updated_at)
  VALUES (
    '00000000-0000-0000-0000-000000000101'::uuid,
    '00000000-0000-0000-0000-000000000001'::uuid,
    'SKU-RED-SHIRT',
    'PHYSICAL',
    true,
    now(),
    now()
  )
  ON CONFLICT (id) DO NOTHING;

  -- Product 2
  INSERT INTO shopizer.product (id, merchant_store_id, sku, type, is_available, created_at, updated_at)
  VALUES (
    '00000000-0000-0000-0000-000000000102'::uuid,
    '00000000-0000-0000-0000-000000000001'::uuid,
    'SKU-BLUE-MUG',
    'PHYSICAL',
    true,
    now(),
    now()
  )
  ON CONFLICT (id) DO NOTHING;
END $$;

COMMIT;

-- Flyway Migration: V2__inventory_seed_mvp
-- Service: inventory-service
-- Purpose:
--   Minimal, idempotent seed inventory rows for MVP.
--
-- IMPORTANT:
-- - inventory-service schema uses table "inventory_stock" WITHOUT "shopizer." prefix.
-- - store_id is a BIGINT (checkout uses a stable hash mapping from UUID -> long for now).
--
-- For the docs happy-path, we standardize on storeId=1 (long) so it matches tax-service defaults.

INSERT INTO inventory_stock (store_id, sku, on_hand, reserved, version)
VALUES
  (1, 'SKU-RED-SHIRT', 100, 0, 0),
  (1, 'SKU-BLUE-MUG',  100, 0, 0)
ON CONFLICT (store_id, sku) DO NOTHING;

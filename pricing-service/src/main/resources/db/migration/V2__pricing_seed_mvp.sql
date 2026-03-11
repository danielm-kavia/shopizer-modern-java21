-- Flyway Migration: V2__pricing_seed_mvp
-- Service: pricing-service
-- Purpose:
--   Minimal, idempotent seed pricing rows for the MVP.
--
-- Notes:
-- - prices table is keyed by (store_code, sku, currency_code).
-- - We use ON CONFLICT DO NOTHING to keep this migration idempotent.

INSERT INTO shopizer.prices (store_code, sku, currency_code, regular_price, sale_price, sale_start_at, sale_end_at, active)
VALUES
  ('DEFAULT', 'SKU-RED-SHIRT', 'USD', 19.9900, NULL, NULL, NULL, true),
  ('DEFAULT', 'SKU-BLUE-MUG',  'USD', 12.5000, NULL, NULL, NULL, true)
ON CONFLICT (store_code, sku, currency_code) DO NOTHING;

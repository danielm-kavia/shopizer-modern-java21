-- Flyway Migration: V2__updated_at_triggers
-- Purpose:
--   Provide a consistent, reusable mechanism to keep updated_at fresh across tables.
--   This improves debuggability and avoids patchy per-table application logic.

BEGIN;

CREATE SCHEMA IF NOT EXISTS shopizer;

CREATE OR REPLACE FUNCTION shopizer.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply to tables that have updated_at
DO $$
DECLARE
  t RECORD;
BEGIN
  FOR t IN
    SELECT quote_ident(schemaname) AS s, quote_ident(tablename) AS tn
    FROM pg_tables
    WHERE schemaname = 'shopizer'
      AND tablename IN (
        'country','zone','language','currency',
        'merchant_store','integration_module','merchant_configuration',
        'category','category_description',
        'product','product_description','product_availability','product_price','product_price_description',
        'customer',
        'orders','order_product','order_total'
      )
  LOOP
    EXECUTE format('DROP TRIGGER IF EXISTS trg_set_updated_at_%s ON %s.%s;', t.tn, t.s, t.tn);
    EXECUTE format(
      'CREATE TRIGGER trg_set_updated_at_%s BEFORE UPDATE ON %s.%s FOR EACH ROW EXECUTE FUNCTION shopizer.set_updated_at();',
      t.tn, t.s, t.tn
    );
  END LOOP;
END$$;

COMMIT;

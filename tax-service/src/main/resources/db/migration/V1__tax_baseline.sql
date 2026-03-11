-- Baseline schema for tax-service (Phase 1).
-- Uses shared schema "shopizer" (configured via spring.flyway.*).

CREATE TABLE IF NOT EXISTS shopizer.store_tax_rate (
  store_id BIGINT PRIMARY KEY,
  -- rate is stored as a fraction (e.g. 0.0825 = 8.25%)
  rate NUMERIC(9,6) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Seed a couple of stores for local/dev convenience.
-- In real deployments these would be managed via an admin API or reference data migration.
INSERT INTO shopizer.store_tax_rate (store_id, rate)
VALUES
  (1, 0.082500),
  (2, 0.050000)
ON CONFLICT (store_id) DO UPDATE
SET rate = EXCLUDED.rate,
    updated_at = now();

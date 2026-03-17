-- Promotions (coupon) baseline schema for promotions-service.
-- Stored under the shared "shopizer" schema.

CREATE TABLE IF NOT EXISTS shopizer.coupon_definition (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  type VARCHAR(16) NOT NULL, -- PERCENTAGE | FIXED_AMOUNT
  percentage_off NUMERIC(7, 4) NULL, -- e.g. 10.0000 for 10%
  amount_off NUMERIC(19, 4) NULL, -- fixed amount in the order currency
  currency VARCHAR(3) NULL, -- required for FIXED_AMOUNT
  active BOOLEAN NOT NULL DEFAULT TRUE,
  starts_at TIMESTAMPTZ NULL,
  ends_at TIMESTAMPTZ NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_coupon_type_fields CHECK (
    (type = 'PERCENTAGE' AND percentage_off IS NOT NULL AND amount_off IS NULL AND currency IS NULL)
    OR
    (type = 'FIXED_AMOUNT' AND amount_off IS NOT NULL AND currency IS NOT NULL AND percentage_off IS NULL)
  )
);

CREATE INDEX IF NOT EXISTS idx_coupon_definition_code ON shopizer.coupon_definition (code);
CREATE INDEX IF NOT EXISTS idx_coupon_definition_active ON shopizer.coupon_definition (active);

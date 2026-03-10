-- Phase 1 (rate quotes only): minimal persistence to support debugging/observability.
-- This is intentionally a log/audit table, not a domain-of-record.
CREATE TABLE IF NOT EXISTS shopizer.shipping_quote_request_log (
  id BIGSERIAL PRIMARY KEY,
  request_id UUID NOT NULL,
  customer_id VARCHAR(64),
  destination_country VARCHAR(2) NOT NULL,
  destination_postal_code VARCHAR(32),
  currency VARCHAR(3) NOT NULL,
  total_weight_grams INTEGER NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  request_json TEXT NOT NULL,
  response_json TEXT
);

CREATE INDEX IF NOT EXISTS idx_shipping_quote_request_log_request_id
  ON shopizer.shipping_quote_request_log (request_id);

CREATE INDEX IF NOT EXISTS idx_shipping_quote_request_log_created_at
  ON shopizer.shipping_quote_request_log (created_at);

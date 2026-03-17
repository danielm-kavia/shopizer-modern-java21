-- Flyway Migration: V1__payment_baseline
-- Service: payment-service
-- Target: PostgreSQL 16
--
-- Purpose:
--   Establish tables for authorize-only payment flow:
--     - payment_intent: our internal "intent" record linked to order/store/customer
--     - payment_authorization: provider authorization details (PayPal)
--
-- Notes:
--   - Uses UUID PK with pgcrypto gen_random_uuid().
--   - Stores monetary values as minor currency units (e.g., cents).
--   - This is Phase 1 (authorize only). Capture/refund tables can be added later.

BEGIN;

CREATE SCHEMA IF NOT EXISTS shopizer;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS shopizer.payment_intent (
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_store_id  UUID NOT NULL,
  order_id           UUID NOT NULL,
  customer_id        UUID,
  provider           VARCHAR(32) NOT NULL, -- e.g. PAYPAL
  currency           CHAR(3) NOT NULL,
  amount_minor       BIGINT NOT NULL CHECK (amount_minor >= 0),
  status             VARCHAR(32) NOT NULL DEFAULT 'CREATED',
  idempotency_key    VARCHAR(128) NOT NULL,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Idempotency guarantee per store + key (caller must supply stable key)
CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_intent_store_idempotency
  ON shopizer.payment_intent (merchant_store_id, idempotency_key);

CREATE INDEX IF NOT EXISTS ix_payment_intent_order
  ON shopizer.payment_intent (merchant_store_id, order_id);

CREATE TABLE IF NOT EXISTS shopizer.payment_authorization (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  payment_intent_id      UUID NOT NULL REFERENCES shopizer.payment_intent(id) ON DELETE CASCADE,
  provider               VARCHAR(32) NOT NULL, -- PAYPAL
  provider_order_id      VARCHAR(128),         -- PayPal order id
  provider_authorize_id  VARCHAR(128),         -- PayPal authorization id
  status                 VARCHAR(32) NOT NULL DEFAULT 'AUTHORIZED',
  raw_response           TEXT,                 -- store provider payload for traceability
  created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_auth_intent_provider
  ON shopizer.payment_authorization (payment_intent_id, provider);

COMMIT;

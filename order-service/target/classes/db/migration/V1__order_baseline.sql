-- Flyway Migration: V1__order_baseline
-- Service: order-service
-- Target: PostgreSQL 16
--
-- Purpose:
--   Establish order tables (orders + order items) in schema "shopizer".
--
-- Notes:
--   - Uses UUID PK with pgcrypto gen_random_uuid().
--   - Includes created_at/updated_at timestamps.

BEGIN;

CREATE SCHEMA IF NOT EXISTS shopizer;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS shopizer.orders (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_store_id UUID NOT NULL,
  customer_id       UUID NOT NULL,
  currency          CHAR(3) NOT NULL,
  status            VARCHAR(32) NOT NULL DEFAULT 'CREATED',
  payment_status    VARCHAR(32) NOT NULL DEFAULT 'UNPAID',
  -- Store monetary values as minor currency units (e.g., cents) to avoid floating point issues
  total_amount      BIGINT NOT NULL DEFAULT 0,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_orders_store_customer_created_at
  ON shopizer.orders (merchant_store_id, customer_id, created_at DESC);

CREATE TABLE IF NOT EXISTS shopizer.order_item (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id    UUID NOT NULL REFERENCES shopizer.orders(id) ON DELETE CASCADE,
  product_id  UUID NOT NULL,
  quantity    INTEGER NOT NULL CHECK (quantity > 0),
  unit_amount BIGINT NOT NULL CHECK (unit_amount >= 0),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_order_item_order_id ON shopizer.order_item (order_id);

COMMIT;

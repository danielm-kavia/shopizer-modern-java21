-- Flyway Migration: V1__cart_baseline
-- Service: cart-service
-- Target: PostgreSQL 16
--
-- Purpose:
--   Establish cart tables (cart + cart items) in schema "shopizer".
--
-- Notes:
--   - Uses UUID PK with pgcrypto gen_random_uuid().
--   - Includes created_at/updated_at timestamps.
--   - Enforces a single ACTIVE cart per (merchant_store_id, customer_id) via partial unique index.

BEGIN;

CREATE SCHEMA IF NOT EXISTS shopizer;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS shopizer.cart (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_store_id UUID NOT NULL,
  customer_id       UUID NOT NULL,
  currency          CHAR(3) NOT NULL,
  status            VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- One active cart per store + customer
CREATE UNIQUE INDEX IF NOT EXISTS uq_cart_active_store_customer
  ON shopizer.cart (merchant_store_id, customer_id)
  WHERE status = 'ACTIVE';

CREATE TABLE IF NOT EXISTS shopizer.cart_item (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  cart_id    UUID NOT NULL REFERENCES shopizer.cart(id) ON DELETE CASCADE,
  product_id UUID NOT NULL,
  quantity   INTEGER NOT NULL CHECK (quantity >= 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- One row per product in a cart
CREATE UNIQUE INDEX IF NOT EXISTS uq_cart_item_cart_product
  ON shopizer.cart_item (cart_id, product_id);

CREATE INDEX IF NOT EXISTS ix_cart_item_cart_id ON shopizer.cart_item (cart_id);

COMMIT;

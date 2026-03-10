-- Flyway Migration: V1__init_schema
-- Target: PostgreSQL 16
--
-- Purpose:
--   Establish a modernized baseline schema for Shopizer (Java 21 target).
--   This is informed by legacy entities described in docs/legacy-data-model.md:
--     MerchantStore, Language/Currency/Country/Zone, Customer, Product/Category,
--     Product pricing/availability, Orders + line items + totals + status history,
--     and MerchantConfiguration / IntegrationModule.
--
-- Notes:
--   - We use UUID primary keys as the modern default.
--   - We keep legacy "code" fields (store_code, product_sku, etc.) with unique constraints.
--   - We add created_at/updated_at timestamps for debuggability.
--   - We keep JSONB columns for configurations/details where legacy used CLOB JSON,
--     but do not assume encrypted blob behavior in the new system.

BEGIN;

CREATE SCHEMA IF NOT EXISTS shopizer;

-- Optional: deterministic UUID generation inside Postgres
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- Reference / tenant tables
-- =========================

CREATE TABLE IF NOT EXISTS shopizer.country (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  iso_code        VARCHAR(2) NOT NULL UNIQUE,
  name            TEXT NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS shopizer.zone (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  country_id      UUID NOT NULL REFERENCES shopizer.country(id),
  code            VARCHAR(32) NOT NULL,
  name            TEXT NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_zone_country_code UNIQUE (country_id, code)
);

CREATE TABLE IF NOT EXISTS shopizer.language (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code            VARCHAR(10) NOT NULL UNIQUE, -- e.g. en, fr, en-US
  name            TEXT NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS shopizer.currency (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code            VARCHAR(3) NOT NULL UNIQUE, -- ISO-4217
  name            TEXT NOT NULL,
  symbol          TEXT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS shopizer.merchant_store (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  store_code            VARCHAR(64) NOT NULL UNIQUE,
  store_name            TEXT NOT NULL,
  country_id            UUID NOT NULL REFERENCES shopizer.country(id),
  zone_id               UUID NULL REFERENCES shopizer.zone(id),
  default_language_id   UUID NOT NULL REFERENCES shopizer.language(id),
  currency_id           UUID NOT NULL REFERENCES shopizer.currency(id),
  use_cache             BOOLEAN NOT NULL DEFAULT FALSE,
  is_active             BOOLEAN NOT NULL DEFAULT TRUE,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS shopizer.merchant_store_supported_language (
  merchant_store_id UUID NOT NULL REFERENCES shopizer.merchant_store(id) ON DELETE CASCADE,
  language_id       UUID NOT NULL REFERENCES shopizer.language(id),
  PRIMARY KEY (merchant_store_id, language_id)
);

-- =========================
-- System / configuration
-- =========================

CREATE TABLE IF NOT EXISTS shopizer.integration_module (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  module_type     VARCHAR(64) NOT NULL, -- legacy: MODULE (e.g. PAYMENT, SHIPPING, ...)
  code            VARCHAR(128) NOT NULL,
  regions         TEXT NULL,
  details         JSONB NULL,
  configuration   JSONB NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_integration_module_type_code UNIQUE (module_type, code)
);

CREATE TABLE IF NOT EXISTS shopizer.merchant_configuration (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_store_id UUID NOT NULL REFERENCES shopizer.merchant_store(id) ON DELETE CASCADE,
  config_key        VARCHAR(128) NOT NULL,
  config_value      JSONB NULL,
  is_active         BOOLEAN NOT NULL DEFAULT TRUE,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_merchant_configuration_key UNIQUE (merchant_store_id, config_key)
);

-- =========================
-- Catalog
-- =========================

CREATE TABLE IF NOT EXISTS shopizer.category (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_store_id UUID NOT NULL REFERENCES shopizer.merchant_store(id) ON DELETE CASCADE,
  code              VARCHAR(128) NULL,
  sort_order        INTEGER NOT NULL DEFAULT 0,
  is_visible        BOOLEAN NOT NULL DEFAULT TRUE,
  parent_id         UUID NULL REFERENCES shopizer.category(id) ON DELETE SET NULL,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_category_store_code UNIQUE (merchant_store_id, code)
);

CREATE TABLE IF NOT EXISTS shopizer.category_description (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  category_id  UUID NOT NULL REFERENCES shopizer.category(id) ON DELETE CASCADE,
  language_id  UUID NOT NULL REFERENCES shopizer.language(id),
  name         TEXT NOT NULL,
  description  TEXT NULL,
  friendly_url TEXT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_category_desc_lang UNIQUE (category_id, language_id)
);

CREATE TABLE IF NOT EXISTS shopizer.product (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_store_id UUID NOT NULL REFERENCES shopizer.merchant_store(id) ON DELETE CASCADE,
  sku               VARCHAR(128) NOT NULL,
  type              VARCHAR(64) NULL,
  is_available      BOOLEAN NOT NULL DEFAULT TRUE,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_product_store_sku UNIQUE (merchant_store_id, sku)
);

CREATE TABLE IF NOT EXISTS shopizer.product_description (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id   UUID NOT NULL REFERENCES shopizer.product(id) ON DELETE CASCADE,
  language_id  UUID NOT NULL REFERENCES shopizer.language(id),
  name         TEXT NOT NULL,
  description  TEXT NULL,
  friendly_url TEXT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_product_desc_lang UNIQUE (product_id, language_id)
);

CREATE TABLE IF NOT EXISTS shopizer.product_category (
  product_id  UUID NOT NULL REFERENCES shopizer.product(id) ON DELETE CASCADE,
  category_id UUID NOT NULL REFERENCES shopizer.category(id) ON DELETE CASCADE,
  PRIMARY KEY (product_id, category_id)
);

CREATE TABLE IF NOT EXISTS shopizer.product_availability (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id    UUID NOT NULL REFERENCES shopizer.product(id) ON DELETE CASCADE,
  region        VARCHAR(64) NULL,
  quantity      INTEGER NOT NULL DEFAULT 0,
  quantity_ordered INTEGER NOT NULL DEFAULT 0,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS shopizer.product_price (
  id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  availability_id         UUID NOT NULL REFERENCES shopizer.product_availability(id) ON DELETE CASCADE,
  price_code              VARCHAR(64) NOT NULL DEFAULT 'base',
  amount                  NUMERIC(19,4) NOT NULL,
  special_amount          NUMERIC(19,4) NULL,
  special_start_date      TIMESTAMPTZ NULL,
  special_end_date        TIMESTAMPTZ NULL,
  created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_price_availability_code UNIQUE (availability_id, price_code)
);

CREATE TABLE IF NOT EXISTS shopizer.product_price_description (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_price_id UUID NOT NULL REFERENCES shopizer.product_price(id) ON DELETE CASCADE,
  language_id      UUID NOT NULL REFERENCES shopizer.language(id),
  description      TEXT NULL,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_price_desc_lang UNIQUE (product_price_id, language_id)
);

-- =========================
-- Customer
-- =========================

CREATE TABLE IF NOT EXISTS shopizer.customer (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_store_id UUID NOT NULL REFERENCES shopizer.merchant_store(id) ON DELETE CASCADE,
  email             CITEXT NOT NULL,
  first_name        TEXT NULL,
  last_name         TEXT NULL,
  default_language_id UUID NOT NULL REFERENCES shopizer.language(id),
  -- Addresses: modeled as JSONB to avoid legacy embedded duplication and enable later normalization
  billing_address   JSONB NULL,
  delivery_address  JSONB NULL,
  is_active         BOOLEAN NOT NULL DEFAULT TRUE,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_customer_store_email UNIQUE (merchant_store_id, email)
);

-- =========================
-- Orders
-- =========================

CREATE TABLE IF NOT EXISTS shopizer.orders (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_store_id UUID NOT NULL REFERENCES shopizer.merchant_store(id),
  customer_id       UUID NULL REFERENCES shopizer.customer(id),
  -- legacy used a customerId primitive field; keep an external reference field for resilience
  customer_ref      TEXT NULL,
  order_status      VARCHAR(64) NOT NULL,
  currency_code     VARCHAR(3) NOT NULL,
  payment_module_code  VARCHAR(128) NULL,
  shipping_module_code VARCHAR(128) NULL,
  billing_address   JSONB NULL,
  delivery_address  JSONB NULL,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS shopizer.order_product (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id    UUID NOT NULL REFERENCES shopizer.orders(id) ON DELETE CASCADE,
  product_id  UUID NULL REFERENCES shopizer.product(id),
  sku         VARCHAR(128) NULL,
  product_name TEXT NOT NULL,
  quantity    INTEGER NOT NULL,
  unit_price  NUMERIC(19,4) NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS shopizer.order_total (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id    UUID NOT NULL REFERENCES shopizer.orders(id) ON DELETE CASCADE,
  code        VARCHAR(64) NOT NULL, -- subtotal, tax, shipping, total, discount, etc.
  title       TEXT NULL,
  value       NUMERIC(19,4) NOT NULL,
  sort_order  INTEGER NOT NULL DEFAULT 0,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS shopizer.order_status_history (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id    UUID NOT NULL REFERENCES shopizer.orders(id) ON DELETE CASCADE,
  status      VARCHAR(64) NOT NULL,
  comments    TEXT NULL,
  customer_notified BOOLEAN NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =========================
-- Helpful indexes
-- =========================

CREATE INDEX IF NOT EXISTS idx_category_store ON shopizer.category(merchant_store_id);
CREATE INDEX IF NOT EXISTS idx_product_store ON shopizer.product(merchant_store_id);
CREATE INDEX IF NOT EXISTS idx_orders_store ON shopizer.orders(merchant_store_id);
CREATE INDEX IF NOT EXISTS idx_orders_customer ON shopizer.orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_product_order ON shopizer.order_product(order_id);
CREATE INDEX IF NOT EXISTS idx_order_total_order ON shopizer.order_total(order_id);
CREATE INDEX IF NOT EXISTS idx_order_status_history_order ON shopizer.order_status_history(order_id);

COMMIT;

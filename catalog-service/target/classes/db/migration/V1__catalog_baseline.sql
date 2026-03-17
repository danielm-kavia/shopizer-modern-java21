-- Flyway Migration: V1__catalog_baseline
-- Service: catalog-service
-- Target: PostgreSQL 16
--
-- Purpose:
--   Establish catalog tables (category/product + localized descriptions) in schema "shopizer".
--
-- Notes:
--   - Uses UUID PK with pgcrypto gen_random_uuid().
--   - Includes created_at/updated_at timestamps (updated_at triggers are handled by infra migration V2 if used centrally).

BEGIN;

CREATE SCHEMA IF NOT EXISTS shopizer;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS shopizer.category (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_store_id UUID NOT NULL,
  code              VARCHAR(128) NULL,
  sort_order        INTEGER NOT NULL DEFAULT 0,
  is_visible        BOOLEAN NOT NULL DEFAULT TRUE,
  parent_id         UUID NULL,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_category_store_code UNIQUE (merchant_store_id, code),
  CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES shopizer.category(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS shopizer.category_description (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  category_id  UUID NOT NULL REFERENCES shopizer.category(id) ON DELETE CASCADE,
  language_id  UUID NOT NULL,
  name         TEXT NOT NULL,
  description  TEXT NULL,
  friendly_url TEXT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_category_desc_lang UNIQUE (category_id, language_id)
);

CREATE TABLE IF NOT EXISTS shopizer.product (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  merchant_store_id UUID NOT NULL,
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
  language_id  UUID NOT NULL,
  name         TEXT NOT NULL,
  description  TEXT NULL,
  friendly_url TEXT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_product_desc_lang UNIQUE (product_id, language_id)
);

COMMIT;

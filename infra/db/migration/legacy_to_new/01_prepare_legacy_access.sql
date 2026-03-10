-- 01_prepare_legacy_access.sql
--
-- Purpose:
--   Prepare helper structures to support deterministic, debuggable migration:
--   - a `legacy` schema placeholder (for dump or FDW foreign tables)
--   - id mapping tables from legacy integer IDs to new UUIDs
--
-- Contract:
--   - Does NOT require legacy tables to exist yet.
--   - Safe to re-run (uses IF NOT EXISTS).
--
-- Recommended:
--   Load legacy tables into schema `legacy` OR create foreign tables in schema `legacy`.

BEGIN;

CREATE SCHEMA IF NOT EXISTS legacy;

CREATE SCHEMA IF NOT EXISTS shopizer_migration;

-- Legacy integer IDs -> new UUIDs
CREATE TABLE IF NOT EXISTS shopizer_migration.map_merchant_store (
  legacy_id BIGINT PRIMARY KEY,
  new_id    UUID NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS shopizer_migration.map_language (
  legacy_id BIGINT PRIMARY KEY,
  new_id    UUID NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS shopizer_migration.map_currency (
  legacy_id BIGINT PRIMARY KEY,
  new_id    UUID NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS shopizer_migration.map_country (
  legacy_id BIGINT PRIMARY KEY,
  new_id    UUID NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS shopizer_migration.map_zone (
  legacy_id BIGINT PRIMARY KEY,
  new_id    UUID NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS shopizer_migration.map_category (
  legacy_id BIGINT PRIMARY KEY,
  new_id    UUID NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS shopizer_migration.map_product (
  legacy_id BIGINT PRIMARY KEY,
  new_id    UUID NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS shopizer_migration.map_customer (
  legacy_id BIGINT PRIMARY KEY,
  new_id    UUID NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS shopizer_migration.map_orders (
  legacy_id BIGINT PRIMARY KEY,
  new_id    UUID NOT NULL UNIQUE
);

COMMIT;

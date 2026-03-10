# Legacy (Shopizer 2.x) → New (shopizer-modern-java21) data migration

## What this folder is
This folder contains SQL templates intended to migrate data from the legacy Shopizer schema (commonly `SALESMANAGER.*`) into the new schema created by Flyway under `shopizer.*`.

Because this repo snapshot only includes documentation (no live legacy DB, no dump, no connection details), the scripts are provided as **operators’ templates** with clear assumptions.

## Contract (assumptions)
Inputs:
- Legacy tables are accessible as either:
  1) A Postgres schema named `legacy` containing legacy tables loaded from a dump, OR
  2) Foreign tables in schema `legacy` (e.g., via `postgres_fdw`).

Outputs:
- Data inserted into `shopizer.*` tables.

Invariants:
- `shopizer.merchant_store.store_code` remains unique.
- `shopizer.product.sku` remains unique per store.
- `shopizer.customer.email` remains unique per store.

Failure modes:
- Missing legacy tables/columns (script must be adjusted to the actual legacy DDL export).
- Non-unique legacy codes/emails causing new unique constraint violations.
- Missing reference data (countries/languages/currencies) for stores/customers.

## How to use
1. Ensure you have a running Postgres (see `infra/db/docker-compose.yml`).
2. Load legacy schema into Postgres as schema `legacy` *or* create FDW mappings.
3. Run the scripts in order:
   - `01_prepare_legacy_access.sql`
   - `02_migrate_reference_and_store.sql`
   - `03_migrate_catalog.sql`
   - `04_migrate_customers.sql`
   - `05_migrate_orders.sql`

## IMPORTANT
The legacy docs indicate table names like `MERCHANT_STORE`, `PRODUCT`, `CATEGORY`, `CUSTOMER`, `ORDERS`, `ORDER_PRODUCT`, `ORDER_TOTAL`, `ORDER_STATUS_HISTORY`, `MERCHANT_CONFIGURATION`, `MODULE_CONFIGURATION`.
However, exact columns/types are not fully captured in the docs. You must reconcile these scripts with a real legacy DDL export.

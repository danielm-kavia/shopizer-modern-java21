# Local PostgreSQL 16 (shopizer-modern-java21)

## Start / stop
From repository root:

```bash
docker compose -f infra/db/docker-compose.yml up -d
docker compose -f infra/db/docker-compose.yml down
```

Postgres will listen on `localhost:5432` with local dev defaults:

- DB: `shopizer`
- User: `shopizer`
- Password: `shopizer`

## Schema migrations (Flyway)
Flyway migration scripts are located at:

- `infra/db/flyway/sql/`

These are plain SQL Flyway migrations (`V*_*.sql`) targeting PostgreSQL 16.

## Legacy → new migration helpers
Scripts to assist migrating data from the legacy Shopizer 2.x schema (typically `SALESMANAGER.*`) into the new schema are located at:

- `infra/db/migration/legacy_to_new/`

These scripts are *templates/starters* because the legacy schema export/connection details are not present in this repo snapshot.
They assume you can access legacy tables (either via `postgres_fdw`, or by loading a legacy dump into a separate schema/database).

# How to find your Shopizer Modern store UUID (`storeId`)

Many Shopizer Modern endpoints are store-scoped, e.g.:

- `GET /api/v1/catalog/stores/{storeId}/categories`
- `GET /api/v1/catalog/stores/{storeId}/products`

`{storeId}` is the UUID primary key of the row in:

- `shopizer.merchant_store.id`

This repository currently does **not** expose a dedicated “list stores” REST endpoint, so the most reliable ways to get the store UUID are:

## Option A (recommended): Use the deterministic MVP seed store UUID

In the MVP seed migration, the store is inserted with a **fixed UUID**:

- File: `catalog-service/src/main/resources/db/migration/V2__catalog_seed_mvp.sql`
- Store UUID (id): `00000000-0000-0000-0000-000000000001`
- Store code: `DEFAULT`

So in preview/local MVP environments where Flyway migrations have run, you can use:

```text
STORE_ID = 00000000-0000-0000-0000-000000000001
```

Example (categories):

```bash
curl -s "http://<GATEWAY_HOST>/api/v1/catalog/stores/00000000-0000-0000-0000-000000000001/categories?page=0&size=50"
```

If your gateway requires JWT, add the appropriate `Authorization: Bearer ...` header (see `docs/mvp-happy-path.md`).

## Option B: Query Postgres for all store IDs (works for any data set)

If you have access to the Postgres instance, run:

```sql
SELECT id, store_code, store_name
FROM shopizer.merchant_store
ORDER BY store_code;
```

### If you’re running via docker-compose (local dev)

1) Start the stack:

```bash
docker compose up -d --build
```

2) Exec into Postgres and query:

```bash
docker compose exec -T postgres psql -U shopizer -d shopizer \
  -c "select id, store_code, store_name from shopizer.merchant_store order by store_code;"
```

Use the `id` value for `{storeId}`.

## Option C: If you migrated legacy data (legacy_to_new scripts)

If your environment runs the legacy migration scripts under:

- `infra/db/migration/legacy_to_new/02_migrate_reference_and_store.sql`

…then store UUIDs are generated using `gen_random_uuid()` during migration, and stored in `shopizer.merchant_store`.

In that case, **Option B (DB query)** is the correct way to retrieve the UUID.

## Notes / common pitfalls

- The categories controller expects a real UUID (Java `UUID` type). If you pass a store code like `DEFAULT`, Spring will return a 400 due to UUID parsing.
- The gateway OpenAPI is exposed at `/openapi.json` (see `gateway/src/main/resources/application.yml`), but there is currently no “stores” listing endpoint in the API spec to discover store IDs automatically.

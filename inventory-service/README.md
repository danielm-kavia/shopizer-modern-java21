# inventory-service

Inventory service for Shopizer modernization.

## Responsibilities
- Track stock per `(storeId, sku)` with:
  - `on_hand` (total stock)
  - `reserved` (reserved for pending checkout/cart)
- Provide synchronous APIs for reservation lifecycle:
  - `POST /api/inventory/reserve`
  - `POST /api/inventory/release`
  - `GET /api/inventory/stock?storeId=...&sku=...`

## Security
OAuth2 Resource Server (JWT). Configure issuer URI via:
- `KEYCLOAK_ISSUER_URI` (defaults to `http://localhost:8080/realms/shopizer`)

## Database / Flyway
Uses PostgreSQL + Flyway migrations under `src/main/resources/db/migration`.

Env vars (defaults provided for local dev):
- `INVENTORY_DB_URL`
- `INVENTORY_DB_USER`
- `INVENTORY_DB_PASSWORD`

## Gateway routing
Gateway routes `/api/inventory/**` to inventory-service via:
- `INVENTORY_SERVICE_URL` (defaults to `http://localhost:8089`)

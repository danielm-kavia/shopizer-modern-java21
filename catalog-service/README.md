# catalog-service

Spring Boot 3.x (Java 21) Catalog Service module for Shopizer modernization.

## Responsibilities (data ownership)
Owns catalog domain read APIs and schema migrations for:
- Categories + localized descriptions
- Products + localized descriptions

## Security
Configured as **OAuth2 Resource Server (JWT)**; issuer defaults to:
- `KEYCLOAK_ISSUER_URI` (default: `http://localhost:8080/realms/shopizer`)

## Local configuration
`src/main/resources/application.yml` uses env-overridable DB settings:

- `CATALOG_DB_URL` (default: `jdbc:postgresql://localhost:5432/shopizer`)
- `CATALOG_DB_USER` (default: `shopizer`)
- `CATALOG_DB_PASSWORD` (default: `shopizer`)

## OpenAPI
- `/v3/api-docs`
- `/swagger-ui.html`

## Endpoints
- `GET /health`
- `GET /api/v1/catalog/stores/{storeId}/products?page=0&size=20`
- `GET /api/v1/catalog/stores/{storeId}/products/{sku}`
- `GET /api/v1/catalog/stores/{storeId}/categories?page=0&size=50`
- `GET /api/v1/catalog/stores/{storeId}/categories/{categoryId}`

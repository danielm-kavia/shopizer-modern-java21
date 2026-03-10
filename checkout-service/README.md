# checkout-service (Phase 1)

Checkout orchestration service for **Shopizer modernization**.

## Scope (Phase 1)
- Create an **order** from an existing **cart** by calling:
  - `cart-service` (GET cart by id)
  - `order-service` (POST create order)
- No payment/shipping yet.

## API
- `POST /api/checkout/orders` – creates an order from cart items.

## Security
- OAuth2 Resource Server validating JWTs issued by Keycloak.
- Requires `Authorization: Bearer <jwt>` and propagates the same token to downstream services.

## Configuration
- `KEYCLOAK_ISSUER_URI` (defaults to `http://localhost:8080/realms/shopizer`)
- `CART_SERVICE_URL` (defaults to `http://localhost:8084`)
- `ORDER_SERVICE_URL` (defaults to `http://localhost:8085`)
- Optional timeouts:
  - `CART_SERVICE_TIMEOUT_MS` (default `3000`)
  - `ORDER_SERVICE_TIMEOUT_MS` (default `3000`)

## OpenAPI / Swagger UI
- `/swagger-ui.html`
- `/v3/api-docs`

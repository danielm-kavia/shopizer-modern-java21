# checkout-service (Checkout orchestration)

Checkout orchestration service for **Shopizer modernization**. This service exposes a single “checkout” API that orchestrates calls to multiple downstream services to compute totals, reserve stock, quote shipping, create an order, and authorize payment.

The single canonical orchestration entrypoint is implemented in:
- `checkout-service/src/main/java/com/shopizer/checkout/service/CreateOrderFromCartFlow.java`

## Scope (current repo evidence)
The implemented flow performs the following steps, synchronously:
- Fetch cart (`cart-service`)
- Reserve inventory per cart line (`inventory-service`)
- Resolve pricing per SKU and compute subtotal (`pricing-service`)
- Optionally apply coupon and compute discount (`promotions-service`)
- Calculate tax (`tax-service`)
- Quote and select shipping option (`shipping-service`)
- Create order (`order-service`)
- Authorize payment (PayPal authorize-only) (`payment-service`)

Important Phase 1 limitations that are explicitly called out in code:
- Shipping is quoted and selected and returned in the response, but the shipping amount is not included in the computed `total`.
- Order items are persisted with `unitAmount=0` (pricing is not yet persisted into the order model).

## API
- `POST /api/checkout/orders` – orchestrates checkout from a cart and returns `CheckoutResponse`.

## Security
- OAuth2 Resource Server validating JWTs.
- Requires `Authorization: Bearer <jwt>`.
- The inbound Authorization header value is propagated to all downstream service calls.

## Downstream endpoints used by the flow
The following endpoints are called by `checkout-service` (paths are hard-coded in the `*ServiceClient` adapters):
- `cart-service`: `GET /api/v1/carts/{cartId}`
- `inventory-service`: `POST /api/inventory/reserve`
- `pricing-service`: `GET /api/pricing/resolve`
- `promotions-service`: `POST /api/promotions/coupons/apply`
- `tax-service`: `POST /api/tax/calculate`
- `shipping-service`: `POST /api/shipping/quotes`
- `order-service`: `POST /api/orders`
- `payment-service`: `POST /api/payments/paypal/authorize`

## Configuration
Downstream service base URLs and timeouts are configured via `shopizer.clients.*` properties (see `ShopizerClientsProperties`), typically mapped from environment variables in each service’s `application.yml`.

Information not available from current sources: a single consolidated list of env var names across all clients for checkout-service, because the mapping is defined in configuration files rather than in code.

## OpenAPI / Swagger UI
- `/swagger-ui.html`
- `/v3/api-docs`

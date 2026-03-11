# Run the modernized repo locally on Mac (Apple Silicon / M3)

## Overview
This runbook explains how to run the `shopizer-modern-java21` modernized Shopizer stack locally on a MacBook with Apple Silicon (M3). It covers prerequisites, Docker Compose startup/shutdown, retrieving a Keycloak access token, and executing the MVP end-to-end “happy path” via the gateway.

The repository includes a single, runnable Compose stack at the repository root (`docker-compose.yml`) that starts:

- Postgres 16
- Keycloak (seeded realm, client, and demo user)
- Gateway
- All backend services required for the MVP

## Prerequisites (MacBook M3 / Apple Silicon)

### Required software
You need the following installed locally:

- Docker Desktop for Mac (Apple Silicon build), with Docker Compose v2 support
- A shell that can run the commands below (zsh or bash)

You also need one of the following for parsing JSON in the token step:

- Python 3 (recommended because it ships by default on many dev setups), or
- `jq` (optional)

### Verify Docker/Compose are available
From a terminal:

```bash
docker --version
docker compose version
```

If `docker compose` is not available, update Docker Desktop.

### Apple Silicon notes
This stack uses standard images (Postgres 16 and Keycloak 24) and locally built Spring Boot services. On Apple Silicon, the main “gotchas” are generally:

- The first build can take longer because all images and Maven layers must be built/pulled.
- If you have an older Docker Desktop or Rosetta-only configuration, builds may be slow. Prefer the native Apple Silicon Docker Desktop.

## Repository layout (what you will run)
The “single stack” Compose file is at:

- `docker-compose.yml` (repository root)

It includes these relevant port bindings on your host:

- Gateway: http://localhost:8081
- Keycloak: http://localhost:8080
- Postgres: localhost:5432

## Start the full MVP stack (recommended path)

From the repository root (`shopizer-modern-java21/`):

```bash
docker compose up -d --build
```

The first run will take the longest because it must build all service images and initialize volumes.

### Check container status
```bash
docker compose ps
```

Keycloak may take a bit to become healthy on first run because it imports the realm.

### Tail logs (optional)
To watch startup logs:

```bash
docker compose logs -f
```

To watch a specific service:

```bash
docker compose logs -f keycloak
docker compose logs -f gateway
docker compose logs -f checkout-service
```

## Stop / reset

### Stop containers (keep volumes)
```bash
docker compose down
```

### Full reset (drops Postgres data)
This deletes named volumes and will remove any persisted database state:

```bash
docker compose down -v
```

## Keycloak: how auth works in this repo
The Compose stack starts Keycloak and imports the seeded realm export from:

- `infra/keycloak/realm-export/shopizer-realm.json`

That realm export creates:

- Realm: `shopizer`
- Client: `shopizer-local` (public, direct access grants enabled)
- Demo user: `alice` / password `alice`

When running via Docker Compose, services use an internal issuer URL (inside the Docker network):

- `http://keycloak:8080/realms/shopizer`

When you call endpoints from your Mac host, you use:

- Keycloak: `http://localhost:8080`
- Gateway: `http://localhost:8081`

## Retrieve a JWT access token from Keycloak (demo user)

### Option A: using Python (no jq required)
```bash
TOKEN=$(
  curl -s \
    -d "client_id=shopizer-local" \
    -d "grant_type=password" \
    -d "username=alice" \
    -d "password=alice" \
    http://localhost:8080/realms/shopizer/protocol/openid-connect/token \
  | python -c 'import sys, json; print(json.load(sys.stdin)["access_token"])'
)

echo "$TOKEN" | head -c 50 && echo "..."
```

### Option B: using jq
If you prefer jq:

```bash
TOKEN=$(
  curl -s \
    -d "client_id=shopizer-local" \
    -d "grant_type=password" \
    -d "username=alice" \
    -d "password=alice" \
    http://localhost:8080/realms/shopizer/protocol/openid-connect/token \
  | jq -r .access_token
)

echo "$TOKEN" | head -c 50 && echo "..."
```

### Quick token validation call (optional)
Once you have `$TOKEN`, you can validate auth by hitting a protected endpoint through the gateway. For example:

```bash
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/customers/me
```

If you omit the Authorization header, you should get `401 Unauthorized`.

## MVP happy path (end-to-end via Gateway + JWT)

This section is intentionally “copy/paste” and mirrors the repo’s canonical walkthrough in `docs/mvp-happy-path.md`, but is included here so this runbook is complete.

All calls go through the gateway and require a Keycloak JWT.

### 1) Define stable IDs used in the MVP seed data
The MVP seed migrations insert deterministic UUIDs so the examples are reproducible:

```bash
STORE_ID="00000000-0000-0000-0000-000000000001"

PRODUCT_RED_SHIRT_ID="00000000-0000-0000-0000-000000000101"
PRODUCT_BLUE_MUG_ID="00000000-0000-0000-0000-000000000102"
```

### 2) Browse products (catalog-service via gateway)

List products:

```bash
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8081/api/v1/catalog/stores/$STORE_ID/products?page=0&size=20"
```

Get a product detail by SKU:

```bash
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8081/api/v1/catalog/stores/$STORE_ID/products/SKU-RED-SHIRT"
```

### 3) Create/get active cart (cart-service via gateway)
Phase 1 note: cart-service currently requires `customerId` in the request body (it is not derived from the JWT yet).

```bash
CUSTOMER_ID="11111111-1111-1111-1111-111111111111"
```

Create or get an active cart:

```bash
CART_JSON=$(
  curl -s \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"merchantStoreId\": \"$STORE_ID\",
      \"customerId\": \"$CUSTOMER_ID\",
      \"currency\": \"USD\"
    }" \
    http://localhost:8081/api/v1/carts/active
)

echo "$CART_JSON"
```

Extract `cartId`:

```bash
CART_ID=$(python -c 'import sys, json; print(json.load(sys.stdin)["id"])' <<< "$CART_JSON")
echo "CART_ID=$CART_ID"
```

### 4) Add an item to the cart
Add 2x red shirts:

```bash
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"productId\": \"$PRODUCT_RED_SHIRT_ID\",
    \"quantity\": 2
  }" \
  "http://localhost:8081/api/v1/carts/$CART_ID/items"
```

### 5) Checkout (checkout-service via gateway)
Checkout requires a destination for shipping quotes. If you omit `selectedShippingQuote`, checkout-service selects the cheapest quote.

Important note about payment: in the default Compose stack, `payment-service` is configured with `PAYPAL_ENABLED=false` in `docker-compose.yml`. That means payment authorization will not contact PayPal, but the flow is still expected to return an authorization response under the Phase 1 behavior.

```bash
CHECKOUT_JSON=$(
  curl -s \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"cartId\": \"$CART_ID\",
      \"merchantStoreId\": \"$STORE_ID\",
      \"customerId\": \"$CUSTOMER_ID\",
      \"storeCode\": \"DEFAULT\",
      \"couponCode\": \"SAVE10\",
      \"destination\": {
        \"country\": \"US\",
        \"postalCode\": \"94107\",
        \"region\": \"CA\"
      },
      \"defaultItemWeightGrams\": 500
    }" \
    http://localhost:8081/api/checkout/orders
)

echo "$CHECKOUT_JSON"
```

Extract `orderId`:

```bash
ORDER_ID=$(python -c 'import sys, json; print(json.load(sys.stdin)["orderId"])' <<< "$CHECKOUT_JSON")
echo "ORDER_ID=$ORDER_ID"
```

### 6) Fetch the created order (order-service via gateway)
```bash
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8081/api/orders/$ORDER_ID"
```

## Troubleshooting (local dev)

### Keycloak is slow to start / token calls fail
If `curl` to the token endpoint fails, confirm Keycloak is up:

- Keycloak base URL: http://localhost:8080
- Realm discovery: http://localhost:8080/realms/shopizer/.well-known/openid-configuration

Then check:

```bash
docker compose ps
docker compose logs -f keycloak
```

### 401 Unauthorized from the gateway
A `401` typically means the gateway (and/or downstream service) rejected your token:

- Confirm you are calling the gateway at `http://localhost:8081/...` (not the internal Docker hostname).
- Confirm you included `-H "Authorization: Bearer $TOKEN"`.
- Confirm the token is for the `shopizer` realm and was minted by your local Keycloak.

### 5xx during checkout
Checkout orchestrates multiple services; a failure may originate in any downstream call. Start by tailing:

```bash
docker compose logs -f checkout-service
docker compose logs -f gateway
```

### Port conflicts on Mac
If ports 8080, 8081, or 5432 are already in use on your laptop, Docker will fail to start the stack. Either stop the conflicting local service or adjust the port mappings in `docker-compose.yml`.

## References (repo evidence)
- Root compose stack: `docker-compose.yml`
- Keycloak templates: `infra/keycloak/README.md`, `infra/keycloak/docker-compose.yml`, `infra/keycloak/realm-export/shopizer-realm.json`
- MVP walkthrough: `docs/mvp-happy-path.md`
- Repo overview: `README.md`

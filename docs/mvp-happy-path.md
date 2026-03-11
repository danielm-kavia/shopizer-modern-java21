# MVP happy path (Gateway + JWT)

This guide is a **copy/paste** end-to-end walkthrough for the local MVP compose stack:

- Browse catalog
- Create a cart
- Add an item
- Checkout (orchestration)
- Retrieve the created order

All calls go **through the gateway** and use a **Keycloak JWT**.

## 0) Start the stack

From repository root:

```bash
docker compose up -d --build
```

Wait for containers to become healthy (Keycloak may take a bit on first run).

## 1) Get an access token (demo user)

The compose stack imports a realm export which creates:

- Realm: `shopizer`
- Client: `shopizer-local` (public, direct access grants enabled)
- User: `alice` / password `alice`

Fetch a token (password grant):

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

## 2) Define stable IDs used in the MVP seed data

The MVP seed migrations insert deterministic UUIDs so the examples are reproducible.

```bash
STORE_ID="00000000-0000-0000-0000-000000000001"

PRODUCT_RED_SHIRT_ID="00000000-0000-0000-0000-000000000101"
PRODUCT_BLUE_MUG_ID="00000000-0000-0000-0000-000000000102"
```

## 3) Browse products (catalog-service via gateway)

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

## 4) Create/get active cart (cart-service via gateway)

Phase 1 note: cart-service currently requires customerId in the request body (not derived from JWT).

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

Extract cartId:

```bash
CART_ID=$(python -c 'import sys, json; print(json.load(sys.stdin)["id"])' <<< "$CART_JSON")
echo "CART_ID=$CART_ID"
```

## 5) Add an item to the cart

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

## 6) Checkout (checkout-service via gateway)

Checkout requires a destination for shipping quotes.
If you omit `selectedShippingQuote`, checkout-service selects the cheapest quote.

IMPORTANT: For the MVP happy path, `payment-service` is configured with `PAYPAL_ENABLED=false` in `docker-compose.yml`,
so payment authorization will not contact PayPal, but the flow will still return an authorization response from the service's Phase 1 behavior.

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

Extract orderId:

```bash
ORDER_ID=$(python -c 'import sys, json; print(json.load(sys.stdin)["orderId"])' <<< "$CHECKOUT_JSON")
echo "ORDER_ID=$ORDER_ID"
```

## 7) Fetch the created order (order-service via gateway)

```bash
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8081/api/orders/$ORDER_ID"
```

## Troubleshooting

### 401 Unauthorized
- Ensure you are calling via gateway (http://localhost:8081/...)
- Ensure `Authorization: Bearer $TOKEN` is present
- Ensure Keycloak is up and realm import completed

### 5xx during checkout
Checkout orchestrates multiple services. For a quick diagnosis, check:

- Gateway routes in `gateway/src/main/resources/application.yml`
- Service logs (`docker compose logs -f checkout-service`, etc.)

### Payment in MVP
This MVP stack defaults to `PAYPAL_ENABLED=false`. To test real PayPal sandbox authorization, set:

- `PAYPAL_ENABLED=true`
- `PAYPAL_CLIENT_ID=...`
- `PAYPAL_CLIENT_SECRET=...`

on the `payment-service` container and restart.

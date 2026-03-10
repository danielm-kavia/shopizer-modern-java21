# Local Keycloak (shopizer-modern-java21)

## Overview
This folder provides a **local development** Keycloak instance used to issue JWTs for the new Spring Boot 3.x services.

The Keycloak realm is seeded from:

- `infra/keycloak/realm-export/shopizer-realm.json`

It creates:

- Realm: `shopizer`
- Client (public): `shopizer-local`
- Demo user: `alice` / password `alice`

Issuer used by services (default):

- `http://localhost:8080/realms/shopizer`

## Start / stop
From repository root:

```bash
docker compose -f infra/keycloak/docker-compose.yml up -d
docker compose -f infra/keycloak/docker-compose.yml down
```

Keycloak admin console:

- http://localhost:8080/admin
- admin user: `admin`
- password: `admin`

## Get an access token (password grant, local dev)
This uses Keycloak's token endpoint for the seeded realm + client.

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

## Call customer-service via gateway (once apps are running)
Customer-service endpoint (via gateway):

```bash
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/customers/me | jq
```

If you call without a token, you should receive `401 Unauthorized`.

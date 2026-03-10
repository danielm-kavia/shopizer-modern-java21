# shopizer-modern-java21
Shopizer java e-commerce software modernized to Java 21

## Build (multi-module Maven)
From repository root:

```bash
./mvnw -q -DskipTests package
```

Modules (current scaffold):
- `gateway` (Spring Cloud Gateway, port `8081`)
- `customer-service` (Spring Boot REST API, port `8082`)

## Local infrastructure
- PostgreSQL 16: `infra/db/README.md`
- Keycloak (seeded realm): `infra/keycloak/README.md`

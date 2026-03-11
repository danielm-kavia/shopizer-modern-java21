# shopizer-modern-java21

Shopizer e-commerce platform modernized to **Java 21** and **Spring Boot 3.x** as a set of Spring services behind a gateway.

## Build (multi-module Maven)
From repository root:

```bash
./mvnw -q -DskipTests package
```

## Run the MVP locally (single docker-compose stack)
This repository includes a runnable MVP stack:

- Postgres 16
- Keycloak (seeded realm + demo user)
- Gateway + all services

Start everything:

```bash
docker compose up -d --build
```

Stop:

```bash
docker compose down
```

Ports:

- Gateway: http://localhost:8081
- Keycloak: http://localhost:8080
- Postgres: localhost:5432

## End-to-end happy path (Gateway + JWT)
See the copy/paste walkthrough:

- `docs/mvp-happy-path.md`

## Local infrastructure (legacy split stacks)
The `infra/` folder also contains the original split compose templates:

- PostgreSQL 16: `infra/db/README.md`
- Keycloak (seeded realm): `infra/keycloak/README.md`

# shopizer-modern-java21

This container currently provides a **Spring Cloud Gateway** (Java 21 / Spring Boot 3) that listens on **port 3001** and proxies `/api/**` calls to upstream services.

## Readiness / health
- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`

## Configuration (env vars)
- `PORT` (default `3001`)
- `CATALOG_SERVICE_URL` (default `http://catalog-service`)
- `SHOPIZER_URL` (default `http://shopizer:8080`)

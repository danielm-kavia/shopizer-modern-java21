# shopizer-modern-java21 Infrastructure Templates

## Overview
This folder contains **validated local development infrastructure** for the `shopizer-modern-java21` modernization target.

## Local development services
### PostgreSQL 16
- Compose file: `infra/db/docker-compose.yml`
- Docs: `infra/db/README.md`
- Exposes: `localhost:5432`

### Keycloak (OIDC / JWT issuer)
- Compose file: `infra/keycloak/docker-compose.yml`
- Realm seed: `infra/keycloak/realm-export/shopizer-realm.json`
- Docs: `infra/keycloak/README.md`
- Exposes: `localhost:8080`
- Issuer used by services: `http://localhost:8080/realms/shopizer`

## Evidence-first rule
Infrastructure templates should match the build/runtime artifacts in this repository. As additional services are added (Dockerfiles, compose stacks, k8s manifests), update both this README and the `docs/` architecture docs to reference concrete file paths and ports.

# shopizer-modern-java21 Documentation

## Overview
This folder contains living architecture and design documentation for the `shopizer-modern-java21` repository. The intent is to keep these documents synchronized with the repository contents as the modernization effort progresses.

The repository now includes a **multi-module Maven baseline** plus initial Spring Boot 3.x services (`gateway`, `customer-service`) and local dev infrastructure under `infra/` (PostgreSQL 16 and Keycloak). These documents should be kept synchronized with the concrete files and runtime behavior evidenced in the repo.

## Documents
The following documents are maintained in this folder.

### MVP runnable walkthrough
- `mvp-happy-path.md` - Copy/paste end-to-end happy path via gateway + JWT.

### Architecture
The architecture document records what can be concluded from repository evidence and calls out gaps to revisit once code, build configuration, and deployment assets are added.

See `architecture.md`.

### Design and decisions
Information not available from current sources.

When implementation begins, this folder is expected to grow with:
- Architectural decision records (ADRs) describing key choices and tradeoffs.
- Design notes for critical subsystems (for example, checkout, payment, shipping, catalog, and search).
- Operational documentation for local development and deployment.

## How to update these docs
When new files are added (for example, `pom.xml`, `Dockerfile`, `docker-compose.yml`, Kubernetes manifests, or Spring configuration), update the documentation to reference the exact files and the concrete behavior they evidence. Avoid describing intended architecture as if it were already implemented.

# shopizer-modern-java21 Documentation

## Overview
This folder contains living architecture and design documentation for the `shopizer-modern-java21` repository. The intent is to keep these documents synchronized with the repository contents as the modernization effort progresses.

At the time of writing, the repository includes only a minimal `README.md` at the root and placeholder files under `docs/` and `infra/`. Most architectural and infrastructure details are therefore not evidenced yet and are explicitly marked as unavailable in the documents.

## Documents
The following documents are maintained in this folder.

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

# shopizer-modern-java21 Infrastructure Templates

## Overview
This folder is intended to hold infrastructure templates (for example, containerization, local development environments, and deployment manifests) for the `shopizer-modern-java21` modernization target.

## Current state
The current repository snapshot contains only a placeholder file under `infra/` and does not include any of the usual infrastructure assets (for example, `Dockerfile`, `docker-compose.yml`, Helm charts, Terraform, or Kubernetes manifests). As a result, there is no deployable or runnable infrastructure evidenced in this repository yet.

## Intended contents as modernization progresses
As the application code and build configuration are added, this folder should be populated with templates that are directly validated against the repository, such as:
- A `Dockerfile` that builds and runs the Java 21 application artifact.
- A `docker-compose.yml` for local development (application plus required dependencies such as a database).
- Optional Kubernetes manifests or Helm charts for deployment, once the runtime requirements are known.

Each template should clearly document:
- Required environment variables and secrets.
- Exposed ports and health check endpoints.
- How persistence is provisioned (if a database is used).
- How migrations and initialization are handled.

## Evidence-first rule
Do not add templates that assume a build tool, module layout, or runtime entrypoint that is not present in the repository. Once `pom.xml` or `build.gradle` and an application entrypoint exist, templates can be added and then referenced back into `docs/architecture.md`.

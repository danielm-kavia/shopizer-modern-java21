# Shopizer Strangler Fig Migration Roadmap (2.x to Java 21 / Spring Boot 3.x)

## Overview
This roadmap describes a phased “Strangler Fig” modernization of the legacy Shopizer 2.x application into a Java 21 platform built on Spring Boot 3.x microservices behind an API Gateway. It is written to align with what is evidenced in the legacy analysis in this repository and the target architecture assumptions already documented.

The legacy system is a single deployable WAR (`sm-shop`) that contains the web layer (storefront, admin, `/services/**` endpoints) and depends on a shared core library (`sm-core`) that contains the domain model, persistence, service layer, and integration modules (payments, shipping, email, caching, and search). The Strangler approach prioritizes extracting stable HTTP APIs and business capabilities incrementally while the legacy monolith continues to operate for areas not yet migrated.

## Guiding Principles and Assumptions
The roadmap assumes the following target-state directions, as stated in the repository’s target architecture document.

The target will use Java 21 and Spring Boot 3.x services, with PostgreSQL 16 as the system of record and an API Gateway acting as the stable public entrypoint. Authentication and authorization will be modernized away from legacy Spring Security 3.1 URL-interception toward OAuth2/OIDC-based token authentication enforced per service. A dedicated Search Service will replace the legacy embedded/remote “Shopizer Search” stack, and sensitive integration credentials will be managed via a secrets mechanism rather than encrypted JSON blobs stored in database rows.

The roadmap also assumes the primary “seam” for strangling is HTTP routing. The legacy system already exposes a service surface under `/services/**` with a split between protected `/services/private/**` and open `/services/public/**`, which makes it a practical compatibility surface for routing and gradual replacement.

## Phases
Each phase introduces new runtime units and gradually shifts traffic from legacy endpoints to new services through the gateway. Phases are designed to reduce risk by migrating read-heavy and less stateful paths first, then moving to order placement and payment, and finally replacing legacy UI and deprecating the WAR.

### Phase 0: Foundations and the Strangler Edge
In this phase, the system gains a gateway and “compatibility routing” so that new services can be introduced without changing client entrypoints. The goal is to make it safe to move one endpoint at a time.

The core deliverables are: a gateway with request correlation and routing rules, a baseline service template for Spring Boot 3.x on Java 21, and shared platform conventions for logging, health, and configuration. This phase also establishes how tokens/roles map to legacy concepts (admin vs customer roles) so later services can enforce authorization consistently.

### Phase 1: Read-first extraction (Catalog and Customer reads)
This phase extracts read-heavy APIs first, because they are easier to validate, have fewer side effects, and provide immediate value for performance and future UI modernization. The Catalog and Customer domains are also strongly evidenced as core capability clusters in the legacy data model and service layer analysis.

The core deliverables are: a Catalog Service for product/category/pricing reads, a Customer Service for customer profile reads, and gateway routing that serves selected endpoints from the new services while leaving everything else in the legacy monolith.

### Phase 2: Cart and Checkout boundary stabilization (pre-order)
This phase addresses the “shopping cart” seam, which is mission-critical but can be made safer by first defining a stable contract and state model. The intent is to reduce legacy session coupling by moving toward explicit cart persistence and stateless APIs.

The core deliverables are: a Cart Service, a shared pricing/totals approach aligned with the catalog model, and compatibility behavior so that existing legacy flows can still complete checkout while cart handling gradually moves out.

### Phase 3: Orders extraction (order lifecycle, totals, status history)
This phase introduces an Order Service that becomes the system of record for order state transitions. The legacy evidence shows orders are mission-critical and tightly coupled to payment and status history. Extracting orders as a service establishes the central workflow pivot for later payment and fulfillment work.

The core deliverables are: Order Service APIs, a migrated order schema or schema ownership approach in PostgreSQL, and gateway routing for order endpoints. During this phase, the system should also define the event vocabulary for key transitions (order placed, order paid, etc.), even if a full event bus is not adopted immediately.

### Phase 4: Payments and transactions extraction (gateway adapters, capture/refund parity)
This phase replaces the legacy payment orchestration (`PaymentServiceImpl`, `TransactionServiceImpl`) with a Payment Service that owns payment method enablement, gateway adapters, transaction persistence, and capture/refund operations. The legacy system’s pattern of merchant configuration stored as encrypted JSON is a significant modernization risk and should not be migrated as-is.

The core deliverables are: Payment Service with adapter-based gateway integrations, a modern configuration model for payment enablement, and a secure secrets handling approach. Order-to-payment interaction becomes either synchronous API calls or an asynchronous pattern where orders publish events and payment updates flow back via events or callbacks; the selected approach should be consistent within this phase.

### Phase 5: Shipping and fulfillment extraction (rates, carrier adapters, rules)
This phase replaces shipping calculation logic and carrier integrations with a Shipping Service. The legacy evidence suggests both configurable shipping methods and carrier modules, but the complete carrier inventory is not fully enumerated from the currently reviewed sources. The shipping service boundary is still valuable because it reduces blast radius for external integrations.

The core deliverables are: Shipping Service for rate calculation and carrier adapters, a typed per-merchant configuration model, and integration tests that validate rate calculations for representative rules and destinations.

### Phase 6: Search replacement (indexing pipeline and query API)
This phase replaces the legacy search stack, which is evidenced to rely on an older embedded/remote model and legacy Elasticsearch-era dependencies. Search is treated as a replacement project rather than an in-place upgrade.

The core deliverables are: Search Service APIs, an indexing pipeline that consumes catalog data changes, and a search engine deployment choice (for example OpenSearch or Elasticsearch) made explicitly when implementation begins.

### Phase 7: UI migration and legacy retirement
The final phase migrates storefront and admin UI away from the legacy JSP/Tiles stack, which is evidenced as both high risk and largely replaceable. With APIs already strangled behind the gateway, the UI can be rebuilt as a separate frontend (or a BFF + frontend approach) while preserving stable backend contracts.

The core deliverables are: replacement UIs, deprecation of legacy UI endpoints, and retirement of the legacy WAR once remaining endpoints are fully served by the new services.

## Service Migration Order (Recommended)
The service order below is chosen to align with the criticality and risk observations already captured in this repository’s migration analysis.

1. API Gateway (and shared platform conventions), because it is the core Strangler mechanism and stability layer.
2. Catalog Service (read APIs first), because catalog and pricing are mission-critical and high-traffic, but read extraction is comparatively lower risk than writes.
3. Customer Service (read APIs first), because it supports account flows and will be required for later checkout and order placement.
4. Cart Service, because it reduces UI/session coupling and provides a stable pre-order contract.
5. Order Service, because it becomes the core workflow record for checkout and payment integration.
6. Payment Service, because payment is high-risk, integration-heavy, and requires careful parity for capture/refund.
7. Shipping Service, because carrier integrations are volatile and benefit from isolation.
8. Search Service, because the legacy search stack is evidenced as obsolete and should be replaced with an explicit pipeline.
9. Notification Service (email), because it is medium criticality and can be modernized in parallel once ordering/payment events exist.
10. Configuration Service (merchant/store configuration), which can be introduced earlier as needed but becomes increasingly important as payment/shipping/email configuration moves out of legacy “encrypted JSON blob” patterns.

## Routing Strategy at the Gateway
The gateway should route by path prefix first, because the legacy system already separates functionality by URL patterns. The migration analysis and legacy architecture notes evidence the following patterns:

The legacy uses `/services/**` as a service surface with a split between protected `/services/private/**` and open `/services/public/**`. The gateway can first proxy all traffic to legacy, then progressively reroute specific endpoints (for example `/services/public/products/**`) to the new Catalog Service while leaving the remaining `/services/**` endpoints proxied to legacy.

For storefront and admin UI, the gateway can preserve existing legacy paths while the new UIs are introduced on new paths, and only later swap the default routes.

## Data Ownership and Migration Approach (High level)
This roadmap assumes a gradual transition to service-owned data rather than a direct “shared database” long-term model. In early phases, read models may be populated from legacy data exports or replication to reduce risk, but the roadmap’s later phases expect services to own their data in PostgreSQL and expose it through their APIs.

Where legacy configuration is stored as encrypted JSON inside merchant configuration rows, the target should move toward typed configuration data and secrets references. This is particularly important for payment and shipping credentials.

## Risks and Constraints to Track
The following risks are directly evidenced or implied by the repository’s legacy analysis.

The legacy runtime is Servlet 2.5-era WAR deployment and cannot be upgraded in-place to Spring Boot 3.x due to Jakarta namespace breaking changes. The legacy security model is URL-interception-based with distinct admin and customer role concepts, which must be mapped carefully into token-based authorization.

Payment and shipping are integration-heavy and rely on merchant configuration stored in the database (often encrypted JSON). Search is evidenced as using an old embedded/remote configuration and legacy Elasticsearch-era dependencies and should be replaced rather than upgraded.

## References
This roadmap is derived from and should remain consistent with the following repository documents:

- `docs/migration-analysis.md`
- `docs/target-architecture.md`
- `docs/legacy-architecture-overview.md`
- `docs/legacy-service-controller-reference.md`
- `docs/legacy-external-integrations.md`

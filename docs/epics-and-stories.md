# Shopizer Modernization Epics and User Stories (Strangler Fig)

## Overview
This document defines epics and user stories for a phased Strangler Fig migration from legacy Shopizer 2.x (a single WAR `sm-shop` depending on `sm-core`) to a Java 21 / Spring Boot 3.x microservice architecture behind an API Gateway. The phases and scope align with the legacy analysis and the target architecture assumptions documented in this repository.

Stories are written to support incremental delivery where the gateway remains the stable entrypoint and routes traffic either to legacy endpoints or to the newly extracted services.

## Phase 0: Foundations and Strangler Edge

### Epic 0.1: API Gateway as the stable entrypoint
As a platform engineer, I want a gateway in front of legacy and new services so that I can migrate endpoints gradually without changing client entrypoints.

User stories include: as a client, I can call existing legacy endpoints through the gateway and receive the same responses as before; as an operator, I can route a path prefix to a target service using configuration without redeploying clients; as a developer, I can correlate requests across gateway and downstream services using a request ID propagated via headers.

### Epic 0.2: Service template and runtime conventions
As a developer, I want a standard Spring Boot 3.x service template on Java 21 so that each extracted service starts with consistent health checks, configuration, and logging.

User stories include: as a developer, I can scaffold a new service that exposes `/actuator/health` and structured logs; as an operator, I can configure services using environment-based configuration rather than legacy property placeholder patterns; as a developer, I can run a service locally with a predictable port and minimal dependencies.

### Epic 0.3: AuthN/AuthZ baseline mapping
As a security engineer, I want a baseline identity and authorization model compatible with legacy admin and customer role concepts so that new services can enforce access consistently.

User stories include: as a customer, I can call customer-protected APIs with a valid token; as an admin, I can call admin-protected APIs with an admin token; as a developer, I can apply role-based authorization consistently across services, matching legacy separation between admin and customer endpoints.

## Phase 1: Read-first extraction (Catalog and Customer reads)

### Epic 1.1: Catalog Service (read APIs)
As a shopper, I want product and category data served by a new Catalog Service so that the system can scale reads independently from legacy.

User stories include: as a shopper, I can list products and retrieve product details through the gateway with response fields equivalent to legacy behavior; as a shopper, I can browse categories and see category product listings; as a merchandiser, I can view product pricing and availability data consistent with legacy pricing structures.

### Epic 1.2: Customer Service (read APIs)
As a customer, I want my profile and address information served by a new Customer Service so that account-related reads can be migrated safely before checkout is migrated.

User stories include: as a customer, I can retrieve my profile information via a customer-protected API; as a customer, I can retrieve my saved addresses; as an admin, I can retrieve customer profile information via an admin-protected API.

### Epic 1.3: Gateway routing for read endpoints
As a platform engineer, I want the gateway to route selected `/services/**` read endpoints to the new services so that the Strangler Fig can progress endpoint-by-endpoint.

User stories include: as an operator, I can route `/services/public/products/**` to Catalog Service and keep all other endpoints on legacy; as a developer, I can perform canary routing for a percentage of traffic to the new read endpoints; as a QA engineer, I can validate that responses match legacy for representative products and customers.

## Phase 2: Cart and checkout boundary stabilization (pre-order)

### Epic 2.1: Cart Service (state model and APIs)
As a shopper, I want a cart that is managed via explicit APIs so that the checkout workflow can become less coupled to legacy web sessions.

User stories include: as a shopper, I can create a cart and add/remove items; as a shopper, I can update item quantities and see recalculated totals; as a shopper, I can retrieve my active cart on a new device using an identifier rather than relying on legacy session behavior.

### Epic 2.2: Pricing and totals consistency for cart
As a product owner, I want cart totals to match legacy calculations so that migrating cart behavior does not change what customers pay.

User stories include: as a shopper, I see unit prices and line totals consistent with catalog pricing rules; as a shopper, I see order-level totals (subtotal, tax estimates, shipping estimates placeholders where appropriate) computed consistently; as a QA engineer, I can run a suite of cart calculation comparisons against legacy for representative scenarios.

### Epic 2.3: Gateway routing for cart endpoints
As a platform engineer, I want cart endpoints routed to Cart Service while keeping checkout placement in legacy until orders are extracted.

User stories include: as a shopper, I can use cart endpoints through the gateway and still complete checkout via legacy endpoints; as an operator, I can rollback routing to legacy if a cart regression is detected.

## Phase 3: Orders extraction (order lifecycle, totals, status history)

### Epic 3.1: Order Service (order creation and retrieval)
As a shopper, I want order placement and order retrieval to be handled by a dedicated Order Service so that order lifecycle becomes independent from the legacy WAR.

User stories include: as a shopper, I can place an order from an existing cart; as a shopper, I can retrieve my order history; as an admin, I can retrieve an order by ID and view its status history.

### Epic 3.2: Order state model and status history parity
As a business stakeholder, I want order status transitions and history tracked as in legacy so that operational workflows remain consistent.

User stories include: as an admin, I can see order status history entries when an order transitions; as a system, I can enforce allowed state transitions (for example “placed” to “paid” after successful payment); as a QA engineer, I can verify order state transitions against legacy behavior for representative payment flows.

### Epic 3.3: Event vocabulary for order lifecycle
As a platform engineer, I want an event vocabulary for order lifecycle transitions so that downstream services (payment, notification, shipping) can integrate without tight coupling.

User stories include: as a system, an “order placed” event is emitted when an order is created; as a system, an “order paid” event is emitted when payment succeeds; as a downstream service, I can consume these events to trigger notifications or fulfillment steps.

## Phase 4: Payments and transactions extraction (capture/refund parity)

### Epic 4.1: Payment Service (transactions and adapter boundary)
As a payments engineer, I want a Payment Service that owns payment transactions and gateway adapter integrations so that payment logic is isolated and can be modernized safely.

User stories include: as a shopper, I can authorize or authorize-and-capture payment for an order; as an admin, I can capture a previously authorized payment; as an admin, I can refund a captured payment with auditing.

### Epic 4.2: Merchant payment configuration modernization
As an operator, I want payment configuration stored in a typed model with secrets managed securely so that the system does not rely on encrypted JSON blobs stored in database rows.

User stories include: as an admin, I can enable or disable a payment method for a store; as an operator, payment secrets are stored outside the database and referenced securely by the Payment Service; as a security engineer, I can audit changes to payment configuration.

### Epic 4.3: Order-to-payment integration and reconciliation
As a developer, I want a consistent integration between Order Service and Payment Service so that order states reflect actual payment outcomes.

User stories include: as a system, an order transitions to “paid” only after payment capture succeeds; as a system, failed payments are recorded with a reason and do not advance order state; as an operator, I can reconcile payment transactions to orders and investigate failures.

## Phase 5: Shipping and fulfillment extraction (rates, carrier adapters, rules)

### Epic 5.1: Shipping Service (rate calculation API)
As a shopper, I want shipping rates calculated by a dedicated Shipping Service so that shipping logic is independent and easier to extend.

User stories include: as a shopper, I can request shipping rates for a cart and destination; as an admin, I can configure store shipping options and validate them; as a QA engineer, I can validate rate calculations for representative destinations and weights.

### Epic 5.2: Carrier adapters and custom shipping methods
As an integration engineer, I want shipping carrier adapters isolated behind a Shipping Service boundary so that carrier changes do not impact the rest of the platform.

User stories include: as an operator, I can enable a carrier integration per store; as a developer, carrier-specific failures do not cause system-wide failures and are handled with clear error reporting; as a business user, I can configure custom shipping methods (such as flat-rate or weight-based) in a supported way.

### Epic 5.3: Order fulfillment handoff
As a system, I want shipping selections associated with orders so that fulfillment can proceed reliably after checkout.

User stories include: as a shopper, my selected shipping method and cost are recorded on the order; as an admin, I can view shipping details on an order; as a downstream process, shipping information can be used to create fulfillment tasks.

## Phase 6: Search replacement (indexing pipeline and query API)

### Epic 6.1: Search Service (query APIs)
As a shopper, I want product search handled by a dedicated Search Service so that search can be scaled and upgraded independently.

User stories include: as a shopper, I can search products by keyword and receive relevant results; as a shopper, I can filter and sort search results by common fields; as an operator, search queries are observable with metrics and logs.

### Epic 6.2: Indexing pipeline from Catalog to Search
As a platform engineer, I want an indexing pipeline that updates the search index when catalog data changes so that search results remain accurate.

User stories include: as a system, product updates trigger reindexing; as a system, initial index builds can be run for a store; as an operator, indexing failures are visible and can be retried.

### Epic 6.3: Decommission legacy search wiring
As an operator, I want the legacy embedded/remote search integration no longer required so that the system avoids obsolete dependencies and deployment modes.

User stories include: as an operator, search traffic is routed exclusively to Search Service; as a developer, no legacy search initialization behavior is required for the migrated paths.

## Phase 7: UI migration and legacy retirement

### Epic 7.1: Storefront UI replacement (API-driven)
As a shopper, I want a modern storefront UI that uses the new APIs so that the legacy JSP/Tiles stack can be retired.

User stories include: as a shopper, I can browse catalog, manage cart, and checkout through the new UI; as a shopper, my order history is accessible through the new UI; as a product owner, UX changes can be deployed independently from backend releases.

### Epic 7.2: Admin UI replacement (API-driven)
As an admin, I want a modern admin UI using the new services so that administration is decoupled from the legacy monolith.

User stories include: as an admin, I can manage products and categories; as an admin, I can view orders and perform operational actions (refunds, status updates) consistent with authorization policies; as a security engineer, admin actions are audited.

### Epic 7.3: Legacy endpoint deprecation and WAR retirement
As an operator, I want the legacy WAR retired once all critical flows are served by the new services so that we eliminate legacy runtime risks and maintenance cost.

User stories include: as an operator, I can identify which legacy endpoints still receive traffic; as an operator, I can deprecate and remove legacy endpoints with a controlled rollout; as an operator, I can shut down the legacy WAR without impacting core business flows.

## References
This epics/stories set is grounded in the repository’s legacy and target architecture analysis:

- `docs/migration-analysis.md`
- `docs/target-architecture.md`
- `docs/legacy-architecture-overview.md`
- `docs/legacy-service-controller-reference.md`
- `docs/legacy-external-integrations.md`

# shopizer-modern-java21 Target Architecture

## Overview
This document describes the proposed target architecture for modernizing Shopizer 2.x into a Java 21 platform using Spring Boot 3.x and PostgreSQL 16. The goal is to preserve core e-commerce behavior (catalog, cart, checkout, orders, payments, shipping, customer accounts, and administration) while replacing the legacy WAR deployment model, Spring 3.1 era configuration, and obsolete dependencies with a service-oriented architecture that supports independent scaling and safer evolution.

Information about what is already implemented in `shopizer-modern-java21` is not available from current sources because the repository currently contains only documentation placeholders and no application code, build files, or deployment artifacts. As a result, this document is a design target, not a description of existing implementation.

## Architecture Diagram
High-level structural view (described, not embedded): the system is composed of a small set of Spring Boot 3.x microservices, each owning a cohesive business capability and its portion of the data model. A single API Gateway (or edge service) exposes a public HTTP API and routes requests to internal services. PostgreSQL 16 is the system of record; each service has its own schema (or database) to enforce ownership. External integrations such as payment gateways, shipping carriers, SMTP/email, and a search engine are accessed through adapter components owned by the relevant services. Asynchronous events are published for key lifecycle transitions (for example, order placed and payment captured) to decouple workflows.

## Core Components
The proposed core components are the following microservices and platform services.

The microservice boundaries below are chosen to match the capability clusters evidenced in the legacy analysis: `sm-core` contains catalog, cart, customer, orders, payment, shipping, and system configuration; `sm-shop` hosts controllers and UIs. The target breaks those into independently deployable units.

### Edge / platform components
API Gateway (edge service) terminates HTTP(S), applies cross-cutting concerns (rate limits, authentication integration, request correlation), and routes to internal services. It should provide a stable external surface so internal services can evolve without forcing external clients to change.

Identity and Access Management is responsible for user authentication and token issuance. The legacy system uses URL-intercept rules and roles (admin role and customer role) and separate admin/customer authentication services. The target should externalize this to an OAuth2/OIDC provider and express authorization using Spring Security 6 resource-server policies in each service.

### Business microservices
Catalog Service owns products, categories, manufacturers, attributes, availability, and pricing rules needed for presenting product pages and building line items for checkout. This aligns to the largest and most frequently accessed legacy entity set (`Product`, `Category`, related pricing entities) described in the legacy persistence unit.

Customer Service owns customer accounts, addresses, preferences, and customer-group associations. In legacy, customers and admin users are distinct security concerns; the target keeps customer profile data in this service and relies on IAM for authentication.

Cart Service owns cart state and cart calculations. The legacy system contains a shopping cart data model and calculation service; the target isolates this to reduce coupling with the UI and to enable stateless APIs.

Order Service owns order lifecycle, totals, status history, and invoice-related behavior. It is the primary orchestrator for “order placed” transitions and publishes events for downstream services such as payments and fulfillment.

Payment Service owns payment method enablement, payment transaction capture/refund operations, and payment gateway adapters. In legacy, payment configuration is stored in merchant configuration as encrypted JSON and payment workflows persist `Transaction` records; the target keeps transaction persistence within this service and replaces legacy configuration handling with modern secrets management and explicit configuration models.

Shipping Service owns shipping rate calculation, carrier adapters, and packaging rules. Legacy evidence indicates both custom shipping methods and carrier integrations; the target isolates this to reduce blast radius when integrating carriers.

Configuration Service (Merchant/Store Configuration) owns merchant store settings and per-merchant enablement/configuration for modules (payment, shipping, email). In legacy, `MerchantStore` and `MerchantConfiguration` are central; the target provides a typed configuration API and avoids encrypted “blob JSON” where feasible.

Search Service owns indexing and query APIs. The legacy search stack is built around an older embedded/remote model; the target uses a modern external search engine (for example, OpenSearch or Elasticsearch) with an explicit indexing pipeline.

Notification Service (Email) owns outbound notifications, templates, and integration with SMTP or an email provider. Legacy mixes static `email.properties` with per-merchant DB JSON configuration; the target isolates this and standardizes configuration.

### Data and infrastructure components
PostgreSQL 16 is the system of record. Services should own their data via separate schemas (or separate databases), and all database access should occur through the owning service to avoid tight coupling.

An event bus is required if asynchronous workflows are adopted. The specific technology is not evidenced in current sources; common choices include Kafka or RabbitMQ, but selection should be made via an explicit decision once operational requirements are known.

## Data Flow
Data flows are primarily HTTP request/response at the edge, with optional asynchronous event propagation for state changes.

A typical read flow is: client calls the API Gateway, which routes to Catalog Service for product data and to Search Service for search queries. A typical write flow is: client calls Cart Service to build a cart, then calls Order Service to place an order. Order Service validates the request (including stock/pricing checks against Catalog Service if needed), persists the order in PostgreSQL, and then coordinates payment authorization/capture via Payment Service. Once payment is successful, Order Service transitions the order state and publishes an “order paid” event for downstream actions such as notifications.

If asynchronous events are used, services consume only the events they need and never directly write into another service’s database.

## Integration Points
The modernization retains the same general integration categories as the legacy system, but the ownership and adapter boundaries change.

Payment gateways (for example, PayPal) are invoked only by Payment Service. Shipping carriers (for example, UPS/USPS) are invoked only by Shipping Service. SMTP or email providers are invoked only by Notification Service. Search engine integration is owned by Search Service and is not embedded inside the application runtime.

Where the legacy system stored integration configuration as encrypted JSON inside merchant configuration rows, the target should store configuration in typed tables and reference secrets using a secrets manager (for example, environment-injected secrets in deployment or a vault system). This reduces operational risk and improves auditability.

## Technology Stack
The target stack is defined as follows.

The application runtime is Java 21. Each service is a Spring Boot 3.x application, packaged as an executable container image. Services use Spring Web (MVC or WebFlux) for HTTP APIs, Spring Security 6 for authorization as a resource server, and Spring Data (JPA) for persistence.

PostgreSQL 16 is the primary database. Migrations should be managed by a migration tool (for example, Flyway or Liquibase), though the specific tool is not evidenced in current sources and must be selected when implementation begins.

For observability, each service should emit structured logs and expose metrics and health endpoints. Concrete tooling (for example, OpenTelemetry, Prometheus) is not evidenced in current sources.

Search should be implemented using a modern external search engine. The exact engine is not evidenced in current sources; selection should be made based on operational constraints and desired query features.

## Key Design Decisions
The following decisions are fundamental to the target design.

The system moves from a single WAR (`sm-shop`) plus shared core library (`sm-core`) to multiple Spring Boot services. This reduces coupling that is inherent in the legacy architecture where controllers, views, and core services share a single deployment unit.

Each microservice owns its data. The legacy persistence unit contains a broad set of entities in one schema; the target improves autonomy by using separate schemas (or databases) and enforcing “no cross-service writes.”

Legacy configuration patterns (Spring XML, properties with placeholder injection, and encrypted JSON blobs) are replaced by Spring Boot configuration conventions, typed configuration models, and explicit secrets management.

Legacy embedded/remote search wiring is replaced with a dedicated Search Service and an external search engine, better aligned to containerized deployment and modern search capabilities.

## Scalability & Performance
The microservice split enables scaling of read-heavy and write-heavy components independently. Catalog and Search often require horizontal scaling for high read traffic, while Orders and Payments typically require strong consistency and careful throughput management.

Caching should be redesigned rather than porting Ehcache/Infinispan directly. Service-level caches (for example, product detail caching) can be implemented with local caches plus an external cache if required, but the exact approach must be validated once traffic profiles and deployment constraints are known.

The architecture supports eventual consistency where appropriate through events, but payment capture and order state transitions should remain strongly consistent within their service boundaries.

## Security Considerations
Authentication and authorization must be modernized from legacy Spring Security 3.1 URL interception to OAuth2/OIDC-based authentication with token-based access. Each service should validate tokens and enforce role-based access for admin and customer capabilities.

Sensitive integration credentials (payment keys, SMTP passwords, carrier API keys) must not be stored as decryptable blobs in database rows. Instead, they should be stored in a secrets manager or injected as deployment secrets, with the database holding only references and non-sensitive configuration.

Transport must be TLS for all external calls. Internal service-to-service traffic should also be secured according to deployment environment constraints.

The system should include audit logging for privileged admin actions and for financial operations such as payment capture and refund.

## Sources
This document is a design target informed by the legacy analysis and build/runtime evidence:
- `shopizer-modern-java21/docs/legacy-architecture-overview.md`
- `shopizer-modern-java21/docs/legacy-module-package-map.md`
- `shopizer-modern-java21/docs/legacy-data-model.md`
- `shopizer-modern-java21/docs/legacy-service-controller-reference.md`
- `shopizer-modern-java21/docs/legacy-external-integrations.md`
- `shopizer/docs/build-deployment.md`
- `shopizer/sm-core/pom.xml`
- `shopizer/sm-shop/pom.xml`
- `shopizer/sm-shop/src/main/webapp/WEB-INF/web.xml`

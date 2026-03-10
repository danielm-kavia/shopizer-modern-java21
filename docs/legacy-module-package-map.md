# Shopizer 2.x Legacy Module and Package Map

## Overview
This document provides a code-backed map of the legacy Shopizer fork’s Maven modules and the major package areas within each module. Shopizer is organized as a web application module (`sm-shop`) that depends on a shared core library (`sm-core`).

## Maven Modules

### sm-core (JAR)
The `sm-core` module is packaged as a JAR and contains most of the business logic and persistence model.

It defines:
- JPA entities and persistence wiring (via `META-INF/sm-persistence.xml` and Spring XML contexts).
- DAO and service layer implementations for catalog, customers, orders, payments, shipping, taxes, reference data, and system configuration.
- Integration module contracts and implementations for CMS/static content, payments, shipping, and email.

Evidence:
- `shopizer/sm-core/pom.xml` declares dependencies for Spring 3.1, Hibernate 4.1, Ehcache integration, Infinispan, MySQL/H2 drivers, Jackson, JavaMail, and PayPal merchant SDK.

### sm-shop (WAR)
The `sm-shop` module is packaged as a WAR and contains the web entrypoints and UI resources.

It defines:
- Spring MVC configuration (`WEB-INF/spring/appServlet/servlet-context.xml`).
- Spring Security configuration (`WEB-INF/spring/appServlet/shopizer-security.xml`).
- `web.xml` wiring for `DispatcherServlet` and `DelegatingFilterProxy` security filter chain.
- Controllers for storefront (`/shop/**`), admin (`/admin/**`), and service endpoints (`/services/**`).
- Tiles definitions and JSP views under `WEB-INF`, and static assets under `/resources`.

Evidence:
- `shopizer/sm-shop/pom.xml` declares dependencies on `sm-core`, Spring MVC, Spring Security, Tiles, and `recaptcha4j`.

## Major Package Areas

### com.salesmanager.core.business (sm-core)
This is the primary “business domain” package. It is component-scanned by `sm-core`’s `spring-context.xml`.

Within it, the package structure is organized by business capability, typically following a pattern of:
- `model` for entities/value objects
- `dao` for data access
- `service` for service interfaces and implementations

Representative capability areas observed in the repository structure include:
- `catalog` (products, categories, manufacturers, pricing, attributes, relationships, reviews, types)
- `shoppingcart` (cart entities, cart services)
- `order` (order, order products, order totals, statuses, invoice generation)
- `customer` (customer accounts and attributes)
- `payments` (payment service orchestration, transaction persistence, payment modules)
- `shipping` (shipping services and integration modules)
- `tax` (tax classes and rates)
- `reference` (countries, zones, languages, currencies, geozones)
- `user` (users, groups, permissions)
- `system` (merchant configuration, integration modules, system configuration)

### com.salesmanager.core.modules (sm-core)
This package holds module implementations and adapters, including CMS/static content and integration infrastructure. It is also component-scanned by `spring-context.xml`.

Notably, CMS caching uses Infinispan-based managers (for example `CacheManagerImpl`) to store and retrieve content artifacts.

### com.salesmanager.core.utils (sm-core)
This package provides cross-cutting utility components that are used across services, including cache utility wrappers and startup hooks.

Evidence:
- `com.salesmanager.core.utils.CacheUtils` uses Spring’s cache abstraction and exposes helper methods to list and evict cache keys.
- `com.salesmanager.core.utils.ApplicationContextListenerUtils` initializes search on Spring context start by calling `initService()` on the `productSearchService` bean.

### com.salesmanager.web.* (sm-shop)
The web module contains controllers, façade implementations, and view-model/populator logic.

Key sub-areas include:
- `com.salesmanager.web.admin.*` for admin controllers, security services, and admin backing entities.
- `com.salesmanager.web.shop.*` for storefront controllers and customer-facing flows.
- `com.salesmanager.web.services.*` for `/services/**` endpoints and their security handling.
- `com.salesmanager.web.populator.*` for mapping `sm-core` domain entities to web DTOs.
- `com.salesmanager.web.filter.*` for request pre-processing such as store/language selection and session initialization.

## Web Configuration Entry Points

### Servlet wiring
`WEB-INF/web.xml` defines:
- `ContextLoaderListener` for the root Spring context
- `DispatcherServlet` mapped to `/`
- `springSecurityFilterChain` via `DelegatingFilterProxy`

The configuration locations include:
- `classpath:spring/spring-context.xml` (from `sm-core`)
- `/WEB-INF/spring/appServlet/shopizer-properties.xml`
- `/WEB-INF/spring/appServlet/shopizer-security.xml`
- `/WEB-INF/spring/appServlet/servlet-context.xml`

### View layer
`servlet-context.xml` configures:
- Tiles (`tiles-admin.xml` and `tiles-shop.xml`)
- A JSP `InternalResourceViewResolver`
- Static resources mapping `/resources/**`
- Multipart upload support (`CommonsMultipartResolver`)
- Message bundles (`bundles/shopizer`, `bundles/messages`, `bundles/shipping`, `bundles/payment`)

## What to Read Next
If you are new to this codebase and want to follow the most important paths:
1. Start with `WEB-INF/web.xml` and `WEB-INF/spring/appServlet/servlet-context.xml` to understand request handling.
2. Read `WEB-INF/spring/appServlet/shopizer-security.xml` to understand URL-level access control.
3. Read `sm-core/src/main/resources/spring/spring-context.xml` to see the core wiring for persistence, caching, modules, and search.
4. Follow a single capability end-to-end, such as payments (`PaymentServiceImpl`) or email (`EmailServiceImpl`), and then locate the corresponding controllers in `sm-shop`.

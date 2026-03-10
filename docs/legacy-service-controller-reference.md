# Shopizer 2.x Legacy Service Layer

## Overview
Shopizer’s service layer lives primarily in `sm-core` and is assembled via Spring XML configuration and component scanning. Services provide transactional boundaries, encapsulate DAO calls, and coordinate integration module adapters for payments, shipping, email, and CMS/static content.

The web module (`sm-shop`) calls into these services either directly from controllers or through façade classes and populators.

## Service Wiring and Transaction Management

### Spring context composition
`sm-core`’s `spring-context.xml` component-scans:
- `com.salesmanager.core.business`
- `com.salesmanager.core.utils`
- `com.salesmanager.core.modules`

It imports additional contexts for:
- Datasource: `classpath:/spring/datasource-c3p0.xml`
- Cache wiring: `classpath:/spring/shopizer-core-ehcache.xml`
- Core config: `classpath:/spring/shopizer-core-config.xml`
- Module wiring: `classpath:/spring/shopizer-core-modules.xml`
- Search wiring: `classpath:/spring/shopizer-search.xml`

Evidence:
- `sm-core/src/main/resources/spring/spring-context.xml`

### Transaction boundaries
Transactions are enabled using:
- `<tx:annotation-driven />` for standard `@Transactional` usage.
- An explicit AOP advisor applying `txAdvice` to any service implementing `TransactionalAspectAwareService`.

Read methods (`get*`, `list*`, `search*`) are configured as read-only, and other methods roll back for `ServiceException`.

Evidence:
- `sm-core/src/main/resources/spring/spring-context.xml`

### Property placeholder configuration
A `PropertyPlaceholderConfigurer` loads:
- `classpath:database.properties`
- `classpath:email.properties`
- `classpath:configs.properties`

Evidence:
- `sm-core/src/main/resources/spring/spring-context.xml`
- `sm-core/src/main/resources/email.properties`
- `sm-core/src/main/resources/configs.properties`

A production `database.properties` file was not found in the reviewed main resources. A test version exists in `sm-core/src/test/resources/database.properties`.

## Cross-Cutting Services

### Caching (Ehcache via Spring cache abstraction)
The core provides a service-level cache as a Spring `Cache` bean named `serviceCache`, backed by Ehcache and a cache region named `com.shopizer.OBJECT_CACHE`.

- The Ehcache manager is created by `EhCacheManagerFactoryBean` pointing at `classpath:/ehcache/smcore-ehcache.xml`.
- A `Cache` instance is obtained via `EhCacheCacheManager.getCache("com.shopizer.OBJECT_CACHE")`.

Evidence:
- `sm-core/src/main/resources/spring/shopizer-core-ehcache.xml`
- `sm-core/src/main/resources/ehcache/smcore-ehcache.xml`

`CacheUtils` is the primary helper component used to put/get/evict objects and list store-prefixed cache keys.

Evidence:
- `sm-core/src/main/java/com/salesmanager/core/utils/CacheUtils.java`

### CMS/static content caching (Infinispan)
CMS managers are built on Infinispan’s embedded cache manager and tree cache structures. `CacheManagerImpl` initializes a `TreeCache` using a named cache obtained from a singleton `VendorCacheManager`.

Evidence:
- `sm-core/src/main/java/com/salesmanager/core/modules/cms/impl/CacheManagerImpl.java`

This Infinispan-based caching is separate from Ehcache and is used for CMS/static content functionality (e.g., product images and static resources).

### Search initialization
Search is configured via `shopizer-search.xml`, which defines a `SearchClient` and workflows for indexing and querying. The server configuration defaults to:
- `clusterName = shopizer`
- `mode = local`
- remote host/port values present but used only in remote mode

Evidence:
- `sm-core/src/main/resources/spring/shopizer-search.xml`

At runtime startup, `ApplicationContextListenerUtils` listens for `ContextStartedEvent` and calls `initService()` on a bean named `productSearchService`.

Evidence:
- `sm-core/src/main/java/com/salesmanager/core/utils/ApplicationContextListenerUtils.java`

## Core Business Services

### EmailServiceImpl
`EmailServiceImpl` loads per-merchant email configuration from `MerchantConfiguration` using the key `Constants.EMAIL_CONFIG`. It parses the stored JSON into an `EmailConfig` object and then delegates the send operation to `HtmlEmailSender`.

Evidence:
- `sm-core/src/main/java/com/salesmanager/core/business/system/service/EmailServiceImpl.java`

Operationally, this means email transport credentials can be stored in the database on a per-merchant basis, while default SMTP properties may also exist in `email.properties` for baseline configuration.

### PaymentServiceImpl
`PaymentServiceImpl` orchestrates:
- Listing payment modules and filtering by regions/country.
- Reading merchant-scoped payment configurations from `MerchantConfiguration` under the key `PAYMENT`.
- Decrypting stored JSON configuration, parsing it into `IntegrationConfiguration` instances, and validating it with the selected `PaymentModule`.
- Delegating to a `PaymentModule` adapter for `authorize`, `authorizeAndCapture`, `initTransaction`, `capture`, and `refund`.
- Persisting `Transaction` records for non-INIT transactions.
- Updating `Order` status and persisting status history for capture/refund actions.

Evidence:
- `sm-core/src/main/java/com/salesmanager/core/business/payments/service/PaymentServiceImpl.java`

The service depends on:
- `MerchantConfigurationService`
- `ModuleConfigurationService` (for global module definitions)
- `TransactionService`
- `OrderService`
- A Spring-injected map of `paymentModules` keyed by module code
- An `Encryption` component used to encrypt/decrypt the merchant configuration JSON

This design allows a merchant to enable and configure payment providers without code changes, at the cost of complex runtime configuration parsing.

### TransactionServiceImpl
`TransactionServiceImpl` persists transactions and normalizes transaction details by serializing them as JSON strings into the `details` column.

It also provides helper queries to determine:
- Capturable transactions (e.g., prior AUTHORIZE without CAPTURE)
- Refundable transactions (e.g., latest CAPTURE or AUTHORIZECAPTURE that has not been fully refunded)

Evidence:
- `sm-core/src/main/java/com/salesmanager/core/business/payments/service/TransactionServiceImpl.java`

## Service Layer to Web Layer Interaction
The web module’s security configuration explicitly wires user/customer authentication services:
- `UserServicesImpl` for admin users
- `CustomerServicesImpl` for customers

and protects:
- `/admin/**` with role `AUTH`
- `/shop/customer/**` with role `AUTH_CUSTOMER`
- `/services/private/**` with role `AUTH`, while `/services/public/**` is open

Evidence:
- `sm-shop/src/main/webapp/WEB-INF/spring/appServlet/shopizer-security.xml`

Controllers are configured via `servlet-context.xml` and an imported `controllers.xml` (not reviewed here), and typically call into `sm-core` services either directly or through `com.salesmanager.web.*` façades.

## Notes and Follow-ups
Information not available from current sources includes:
- The full content of `shopizer-core-modules.xml` and how all payment/shipping module beans are wired into the `paymentModules` map. This document describes the contract and usage pattern as evidenced in `PaymentServiceImpl`, but the complete module list requires reviewing that Spring XML.
- The exact encryption key usage and rotation strategy. A `secretKey` is present in `configs.properties`, but the complete `Encryption` implementation and how it consumes this key should be reviewed before changing configuration handling.

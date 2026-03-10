# Shopizer 2.x Legacy External Integrations

## Overview
Shopizer integrates with multiple external systems through a mix of:
- Spring-wired module adapters (payment/shipping/CMS)
- Database-stored merchant configuration (often JSON, sometimes encrypted)
- Property files loaded by Spring at startup

This document summarizes the integrations evidenced in the current legacy fork sources.

## Payments

### Integration pattern
Payments are coordinated through `PaymentServiceImpl`, which selects a payment adapter (`PaymentModule`) from a Spring-injected map and executes operations such as authorization, capture, initialization, and refund.

Merchant-scoped enablement and configuration is stored in `MERCHANT_CONFIGURATION` under the key `PAYMENT`, and is decrypted and parsed at runtime into `IntegrationConfiguration` entries.

Evidence:
- `sm-core/src/main/java/com/salesmanager/core/business/payments/service/PaymentServiceImpl.java`
- `sm-core/src/main/java/com/salesmanager/core/business/system/model/MerchantConfiguration.java`
- `sm-core/src/main/java/com/salesmanager/core/business/system/model/IntegrationConfiguration.java`

### PayPal
The core module declares a dependency on PayPal’s merchant SDK (`com.paypal.sdk:merchantsdk`).

Evidence:
- `sm-core/pom.xml`

The web module also includes PayPal Express Checkout URLs as properties in `shopizer-properties.xml` for sandbox and production endpoints. These are URL templates used to redirect users after obtaining a token.

Evidence:
- `sm-shop/src/main/webapp/WEB-INF/spring/appServlet/shopizer-properties.xml`

## Shipping carriers
The repository structure indicates shipping integration modules exist under `com.salesmanager.core.modules.integration.shipping` (not fully enumerated in this documentation pass). Admin controllers include shipping configuration management endpoints, including custom shipping methods configuration.

Evidence:
- `sm-shop/src/main/java/com/salesmanager/web/admin/controller/shipping/CustomShippingMethodsController.java` (discovered in repository index)

A full list of carriers and their configuration schemas requires reviewing module wiring and shipping module implementations.

## Email (SMTP)

### Baseline SMTP properties
`email.properties` provides a sample SMTP configuration (Gmail via SMTPS). These properties are loaded by Spring’s `PropertyPlaceholderConfigurer`.

Evidence:
- `sm-core/src/main/resources/email.properties`
- `sm-core/src/main/resources/spring/spring-context.xml`

### Merchant-specific email configuration
`EmailServiceImpl` stores and retrieves per-merchant email configuration under `Constants.EMAIL_CONFIG` via `MerchantConfigurationService`. The stored value is JSON parsed into `EmailConfig`.

Evidence:
- `sm-core/src/main/java/com/salesmanager/core/business/system/service/EmailServiceImpl.java`

Operationally, this allows a given merchant to have custom SMTP sender configuration stored in the database, independent of the static `email.properties` file.

## Search (Shopizer Search / Elasticsearch-like)

Shopizer uses an internal search library (`sm-search`) and a Spring-defined search workflow. The search configuration defines:
- Embedded “local” mode (default), which will create indices in the working directory.
- Optional “remote” mode via `clusterHost` and `clusterPort` (defaults 127.0.0.1:9300).

Evidence:
- `sm-core/src/main/resources/spring/shopizer-search.xml`
- `sm-core/pom.xml` dependency on `com.shopizer:sm-search`

Search initialization is triggered at Spring context start via `ApplicationContextListenerUtils`, which calls `initService()` on a bean named `productSearchService`.

Evidence:
- `sm-core/src/main/java/com/salesmanager/core/utils/ApplicationContextListenerUtils.java`

## Caching

### Ehcache (service cache + Hibernate second-level)
Shopizer wires an Ehcache-backed Spring cache manager and exposes a cache named `com.shopizer.OBJECT_CACHE`.

Evidence:
- `sm-core/src/main/resources/spring/shopizer-core-ehcache.xml`
- `sm-core/src/main/resources/ehcache/smcore-ehcache.xml`
- `sm-core/src/main/resources/spring/spring-context.xml` (Hibernate cache settings)

`CacheUtils` provides helper operations and includes logic to list keys and evict entries.

Evidence:
- `sm-core/src/main/java/com/salesmanager/core/utils/CacheUtils.java`

### Infinispan (CMS/static content)
CMS caching uses Infinispan’s embedded cache manager and a tree cache abstraction. Managers initialize named caches and expose tree operations.

Evidence:
- `sm-core/src/main/java/com/salesmanager/core/modules/cms/impl/CacheManagerImpl.java`

## CAPTCHA (reCAPTCHA)
The web module includes `recaptcha4j` and also ships public/private key properties in `shopizer-properties.xml`.

Evidence:
- `sm-shop/pom.xml` dependency `net.tanesha.recaptcha4j:recaptcha4j`
- `sm-shop/src/main/webapp/WEB-INF/spring/appServlet/shopizer-properties.xml`

The existence of these keys implies customer registration and/or form submission flows may validate CAPTCHA (the specific controller logic was not reviewed in this pass).

## Authentication and Authorization (Spring Security)
The security model is implemented with Spring Security using URL interception and role checks:
- Admin endpoints under `/admin/**` require `hasRole('AUTH')`.
- Customer “account” endpoints under `/shop/customer/**` require `hasRole('AUTH_CUSTOMER')`.
- Services endpoints under `/services/**` are stateless, with `/services/private/**` requiring `hasRole('AUTH')` and `/services/public/**` open.

Evidence:
- `sm-shop/src/main/webapp/WEB-INF/spring/appServlet/shopizer-security.xml`
- `sm-shop/src/main/webapp/WEB-INF/web.xml` (DelegatingFilterProxy)

## Configuration Storage Summary
The following table summarizes where configuration is stored.

| Area | Storage | Evidence |
|------|---------|----------|
| Payment enablement/keys per merchant | `MERCHANT_CONFIGURATION` (encrypted JSON) | `PaymentServiceImpl`, `MerchantConfiguration` |
| Email config per merchant | `MERCHANT_CONFIGURATION` (JSON) | `EmailServiceImpl` |
| Baseline SMTP settings | `email.properties` | `spring-context.xml` |
| Encryption secret | `configs.properties` (`secretKey`) | `configs.properties` |
| PayPal redirect URLs | `shopizer-properties.xml` | `shopizer-properties.xml` |
| Search mode/cluster | `shopizer-search.xml` | `shopizer-search.xml` |
| Cache regions/timeouts | `smcore-ehcache.xml` | `smcore-ehcache.xml` |

## Follow-ups
A complete external integration inventory (especially shipping carriers) requires inspection of:
- `sm-core/src/main/resources/spring/shopizer-core-modules.xml`
- the concrete module implementations under `sm-core/src/main/java/com/salesmanager/core/modules/integration/`

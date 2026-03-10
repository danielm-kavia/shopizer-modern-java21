# Inventory Service – Requirements Trace (Subtask: inventory-service + gateway wiring)

Source: Work item/subtask text (from task prompt)
- “Implement inventory-service … stock per SKU per store (on_hand, reserved) and synchronous reservation (checkout/cart calls inventory to reserve/release). Add Flyway schema + JPA entities, REST controllers with OpenAPI 3.1 annotations, Spring Security OAuth2 resource server JWT (Keycloak issuer pattern used by other services), and wire Spring Cloud Gateway routing under /api/inventory/**. Place service as a new module inside shopizer-modern-java21 multi-module build and update the parent pom.xml accordingly.”

## Requirement Inventory (minted IDs)
- REQ-001 (Functional): Persist stock per SKU per store with `on_hand` and `reserved`.
- REQ-002 (Functional): Provide synchronous `reserve` API for checkout/cart integration.
- REQ-003 (Functional): Reservation must enforce availability (cannot reserve beyond `on_hand - reserved`).
- REQ-004 (Functional): Provide synchronous `release` API for checkout/cart integration.
- REQ-005 (Constraint): Use Flyway + JPA for persistence.
- REQ-006 (Constraint): Secure APIs with Spring Security OAuth2 Resource Server JWT using Keycloak issuer pattern used by other services.
- REQ-007 (Constraint): Provide OpenAPI documentation.
- REQ-008 (Constraint): Add gateway route `/api/inventory/**`.
- REQ-009 (Constraint): Add new module to parent `pom.xml`.

## Inline Requirement ID Convention
Java single-line comment format:
- `// REQ: REQ-XXX - <short hint>`

## Trace Matrix

| Req ID | Requirement | Source | Implementation Mapping | Inline Code Trace | Verification Mapping | Status | Notes |
|---|---|---|---|---|---|---|---|
| REQ-001 | Persist stock per SKU per store with on_hand/reserved | Work item excerpt above | `inventory-service/src/main/resources/db/migration/V1__inventory_baseline.sql` ; `InventoryStock` entity | `V1__inventory_baseline.sql` comment | Not available from current sources (no tests added in this task) | Partial | DB constraints enforce non-negative and reserved<=on_hand. |
| REQ-002 | Synchronous reserve API | Work item excerpt above | `InventoryController.reserve` ; `InventoryReservationFlow.reserve` | `InventoryController.reserve` ; `InventoryReservationFlow.reserve` | Not available from current sources | Partial | Flow logs start/success for debuggability. |
| REQ-003 | Enforce availability on reserve | Work item excerpt above | `InventoryReservationFlow.reserve` | `InventoryReservationFlow.reserve` | Not available from current sources | Partial | Returns 409 via `ApiExceptionHandler`. |
| REQ-004 | Synchronous release API | Work item excerpt above | `InventoryController.release` ; `InventoryReservationFlow.release` | `InventoryController.release` ; `InventoryReservationFlow.release` | Not available from current sources | Partial | Release disallows releasing > reserved (409). |
| REQ-005 | Flyway + JPA | Work item excerpt above | `inventory-service/pom.xml` ; `application.yml` ; entity/repo classes | N/A | Not available from current sources | Partial | Persistence stack matches other services. |
| REQ-006 | JWT security (Keycloak issuer pattern) | Work item excerpt above | `config/SecurityConfig.java` ; `application.yml` | N/A | Not available from current sources | Partial | Matches existing services’ approach. |
| REQ-007 | OpenAPI documentation | Work item excerpt above | `config/OpenApiConfig.java` ; springdoc dependency | N/A | Not available from current sources | Partial | Uses springdoc like other services. |
| REQ-008 | Gateway route `/api/inventory/**` | Work item excerpt above | `gateway/src/main/resources/application.yml` | N/A | Not available from current sources | Partial | Route uses `INVENTORY_SERVICE_URL` env var with local default. |
| REQ-009 | Add new module to parent pom | Work item excerpt above | `shopizer-modern-java21/pom.xml` | N/A | Not available from current sources | Partial | Enables building module in Maven reactor. |

## Update Rules
1) When a requirement changes: update the Requirement Inventory + Trace Matrix row first, then adjust implementation and inline markers in the same change.
2) When code is refactored (move/rename): update “Implementation Mapping” paths and move inline `// REQ:` comments with the owning entrypoints.
3) When tests are added: update “Verification Mapping” and upgrade Status from Partial to Implemented as appropriate.
4) When new modules are introduced: add/adjust rows if they implement any REQ-00X items.
5) Ownership: keep this file updated alongside inventory-service changes.

## Verification Artifacts Policy
- Preferred: automated tests in `inventory-service/src/test/java/...` validating reserve/release behavior and DB constraints.
- Minimum: reproducible manual verification via running gateway + inventory-service and calling endpoints through `/api/inventory/**`.
- This task did not add tests; verification mapping is currently missing.

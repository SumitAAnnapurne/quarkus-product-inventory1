# Quarkus Product Inventory (Reactive + REST Client)

Minimal Quarkus app that manages products and integrates with an external Price Rules service via Quarkus REST Client Reactive.

## Run
1. Start a mock Price Rules service on localhost:9099 (tests use WireMock automatically).
2. Start app: `mvn quarkus:dev`

## Tests
`mvn test` (WireMock stubs are used for external service)

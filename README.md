# MaquiGest — Platform

**CleanCode · Aplicaciones Open Source · UPC · NRC 7750**

## Overview

MaquiGest Platform is the Spring Boot REST API for construction-equipment rental management. It provides the business rules, authentication, authorization and MySQL persistence for [MaquiGest WebApp](https://github.com/upc-pre-202620-1asi0729-7750-cleancode/maquigest-webapp). The application is organized into six business bounded contexts (`iam`, `profiles`, `inventory`, `rentals`, `maintenance`, `subscriptions`) and a cross-cutting `shared` kernel.

## Features

All paths below are relative to `/api/v1`. Consult OpenAPI for complete request/response contracts and security requirements.

| Context | Main resources and operations |
|---|---|
| IAM | `/authentication/sign-up`, `/authentication/sign-in`, `/authentication/me`, `/authentication/password-recovery`, `/authentication/reset-password`, `/authentication/verify-email`, `/authentication/resend-email-verification`. |
| Profiles | `/profiles`: company profile provisioning, lookup and update. |
| Inventory | `/equipment`, `/equipment-categories`: equipment management, operational status, availability blocks and availability queries. |
| Rentals | `/rental-requests`, `/rentals`, `/deliveries`, `/equipment-returns`: requests, period changes, approval/rejection, delivery and return. `GET /rentals/{rentalId}/locations?address=...`: optional geocoding. |
| Maintenance | `/maintenances`, `/incidents`: maintenance history, scheduling and tracking, incidents and equipment reactivation. |
| Subscriptions | `/subscription-plans`, `/user-subscriptions`, `/subscription-checkouts`: plans, subscription management and Stripe test checkout. |

Each business context owns its notifications: `/iam-notifications`, `/profile-notifications`, `/inventory-notifications`, `/rental-notifications`, `/maintenance-notifications`, `/subscriptions-notifications`. Each supports `GET` collection, `GET /{id}` and `PATCH /{id}/read`, scoped to the authenticated recipient.

## Technologies and prerequisites

- Java 27 (validated with 27.0.1.0.3), Spring Boot 4.1.2, Maven Wrapper 3.9.11.
- MySQL 8 (validated with 8.0.46), Spring Data JPA/Hibernate, Spring Security/JWT, Jakarta Validation, Lombok, springdoc-openapi 3.2.0.
- Git, JDK configured in `JAVA_HOME`, local MySQL server and a user with access to the development schema.
- SendGrid, Stripe Checkout and Google Maps Geocoding are optional external integrations, disabled by default.

## Architecture

The package root is `com.maquigest.platform`. Business contexts are separated into `domain`, `application`, `infrastructure` and `interfaces`. Domain aggregates and value objects encapsulate invariants; domain repositories provide persistence contracts. JPA entities, persistence assemblers and repository adapters live in infrastructure. Public command/query contracts and their implementations are separated using CQRS. REST resources and assemblers translate the public API. ACL ports/facades and published integration events support cross-context coordination. Expected command failures use `Result<T, ApplicationError>` where the application contract provides it; existing REST contracts are preserved.

`shared` supplies cross-cutting infrastructure and application abstractions, not a seventh business context. Notifications are owned by each business context rather than a standalone notification context. Application errors and notification text support English and Spanish; use `Accept-Language: es-PE` to request Spanish where supported.

### Architecture decisions

- [Aggregate persistence boundaries](docs/adr-aggregate-persistence-boundaries.md): delivery/return and maintenance history remain child entities of their lifecycle roots.
- [In-process integration events](docs/adr-in-process-domain-integration-events.md): published integration contracts use after-commit bridges. This historical ADR predates the later notification/email features; those do not introduce a durable outbox or external event broker.
- [Rental request concurrency](docs/adr-rentals-request-concurrency.md): InnoDB guard row serializes overlapping requests. The historical ADR records a proposed status; the opt-in MySQL test has since passed locally. The simultaneous authenticated REST POST/PATCH deployment check remains outstanding.

## Getting started (Windows PowerShell)

```powershell
git clone https://github.com/upc-pre-202620-1asi0729-7750-cleancode/maquigest-platform.git
cd maquigest-platform
git switch develop
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DATABASE_URL = 'localhost'
$env:DATABASE_PORT = '3306'
$env:DATABASE_NAME = 'maquigest-platform'
$env:DATABASE_USER = 'root'
$credential = Get-Credential -UserName $env:DATABASE_USER -Message 'Local MySQL credentials'
$env:DATABASE_PASSWORD = $credential.GetNetworkCredential().Password
$bytes = New-Object byte[] 64
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
.\mvnw.cmd spring-boot:run
```

Use a stable, securely managed JWT secret across restarts if sessions must survive. The example generates a new local-only secret. Do not commit secrets or share their values. After stopping the application, remove sensitive environment variables from the shell. Swagger UI: `http://localhost:8080/swagger-ui/index.html`; OpenAPI: `http://localhost:8080/v3/api-docs`. Register a `rental_company` or `construction_company`, sign in, and use the returned JWT as `Authorization: Bearer <token>` for protected resources. Local frontend: `http://localhost:4200`.

## Configuration

| Environment variable | Purpose / default |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` by default; use `prod` for deployment. |
| `DATABASE_URL`, `DATABASE_PORT`, `DATABASE_NAME`, `DATABASE_USER` | MySQL connection; development defaults `localhost`, `3306`, `maquigest-platform`, `root`. Required in production. |
| `DATABASE_PASSWORD` | Required database password. |
| `JWT_SECRET` | Required signing secret in both profiles. |
| `PORT` | HTTP port, default `8080`. |
| `CORS_ALLOWED_ORIGINS` | Comma-separated permitted origins; local defaults include `http://localhost:4200`. |
| `MAQUIGEST_WEBAPP_BASE_URL` | Frontend origin for recovery, verification and checkout redirects; default `http://localhost:4200`. |
| `MAQUIGEST_EMAIL_ENABLED` | Enable SendGrid transport; default `false`. |
| `SENDGRID_API_KEY`, `SENDGRID_FROM_EMAIL`, `SENDGRID_FROM_NAME` | Verified sender and server-side credentials; name defaults to `MaquiGest`. |
| `MAQUIGEST_EMAIL_LOCALE` | Rental email language; default `es-PE`. |
| `MAQUIGEST_IAM_EMAIL_VERIFICATION_REQUIRED` | Require verification of newly registered accounts; default `false`. Enable only when email delivery is configured. |
| `MAQUIGEST_STRIPE_ENABLED` | Enable Stripe test checkout; default `false`. |
| `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET` | Sandbox server-side key and signing secret. |
| `MAQUIGEST_MAPS_ENABLED`, `GOOGLE_MAPS_API_KEY` | Optional server-side geocoding; disabled by default. |

| Profile | Persistence |
|---|---|
| `dev` | `ddl-auto=update`, database creation allowed, SQL logging enabled. |
| `prod` | `ddl-auto=validate`, database creation disabled, SQL logging off; direct unpaid subscription activation blocked. |

## External integration boundaries

- SendGrid sends rental-request lifecycle emails and IAM recovery/verification links. Delivery occurs outside the originating committed business transaction; no durable retry/outbox guarantee. Recovery and verification tokens are hashed, time-limited and single-use.
- Stripe uses a **test/sandbox-only** one-time Checkout payment for a monthly plan in PEN. Confirmation and signed webhook settlement verify provider data on the server; no recurring billing or real charges. Configure `/api/v1/subscription-checkouts/webhook` for test events `checkout.session.completed`, `checkout.session.expired`, `checkout.session.async_payment_succeeded`.
- Google Maps resolves a planning address for an authorized rental company. Lookup does not persist a delivery/return location or modify rental status. No real-time tracking or routing engine.

## Build and tests

```powershell
.\mvnw.cmd clean verify
```

Run against a dedicated disposable MySQL schema, with external integrations disabled. The Phase 6 baseline `23fb87c` passed 373 tests in the general regression and 11 additional opt-in MySQL tests in complementary runs (384 total, zero failures/errors). The opt-in suites require explicitly enabled test flags and dedicated schema names; inspect their conditions before execution. The production profile was also rehearsed locally on a newly initialized isolated schema, with OpenAPI, JWT and unpaid-checkout protection verified. These are not evidence of a completed cloud migration.

## Deployment and known limitations

`system.properties` declares Java 27. The target backend/MySQL hosting is Northflank; verify its runtime, secrets, backup and schema migration before deploying `prod`. Deploy only after schema validation against the intended updated database. Configure provider credentials and the public frontend origin on the server; never expose keys in Angular. Rotate any previously exposed credentials before deploying. Stripe remains sandbox-only even with `prod` enabled. The npm audit findings in the companion WebApp remain unresolved.

## Version control

Releases use GitFlow and Semantic Versioning; this documentation does not create a release or tag.

# treasury-platform-parent

> **Synthetic demo code.** This repository is a fictional "Harborline Treasury" platform built to demonstrate
> fleet-wide framework upgrades. It is not, and is not derived from, any bank's production code.

Shared build and runtime platform for the treasury services fleet:

| Repo | Role |
|------|------|
| `treasury-platform-parent` (this repo) | Maven parent + BOM, `treasury-starter` |
| `treasury-accounts` | Accounts, transactions, account controls |
| `treasury-payments` | ACH origination with dual-control approval |
| `treasury-balances` | Balance positions, history and alerts |
| `treasury-statements` | Statement generation and downloads |

## What's in here

- **Parent POM / BOM** (`pom.xml`): pins Spring Boot, Java level and shared third-party versions. Every service
  uses `com.harborline.treasury:treasury-platform-parent` as its `<parent>`.
- **`treasury-starter`**: auto-configured platform defaults every service gets by adding one dependency:
  - `X-Correlation-Id` propagation into responses and the logging MDC
  - the shared API error envelope (400 / 404 / 409 semantics)
  - baseline HTTP security: `TREASURY_VIEWER` for reads, `TREASURY_OPERATOR` for state changes, HTTP Basic
  - `treasury.platform.*` configuration properties

## Versions

- Spring Boot 2.7.18, Java 11, Maven 3.9 (wrapper included)

## Build

```bash
./mvnw -B verify          # build and test
./mvnw -B install         # install to ~/.m2 so services can resolve it locally
```

Services' CI checks out this repo and runs `./mvnw install -DskipTests` before building, at the ref named in
the service's `.platform-ref` file (defaults to `main`).

## Local credentials

Development-only in-memory users (override via `treasury.platform.security.*`):

| User | Password | Roles |
|------|----------|-------|
| `treasury-operator` | `operator-local` | operator, viewer |
| `treasury-viewer` | `viewer-local` | viewer |

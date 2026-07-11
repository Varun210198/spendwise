# SpendWise

A personal expense-tracking mobile app. Users register, log expenses against
categories, and generate spend reports (weekly/monthly/yearly/custom range).

This file is the single source of truth for product scope and architecture
decisions. Keep it updated as decisions change — treat it as living
documentation, not a one-time writeup.

## Project layout

SpendWise is split across two repos:

- **`spendwise-api`** (this repo) — Spring Boot backend, REST API + Postgres.
- **`spendwise-mobile`** (separate repo, not yet created) — React Native +
  TypeScript client, consumes this API.

Split rather than a monorepo because the two halves have unrelated
toolchains (Maven vs npm), CI, and release cadence.

## Product scope

- **Single-user personal finance app.** No shared/multi-user expenses, no
  household or group splitting.
- **Expenses only.** No income tracking, no net worth/savings calculations.
- **Single currency.** No multi-currency or conversion.
- **No accounts/wallets.** Transactions aren't tied to a bank/cash/card
  account — just user → category → amount.
- **No budgets or spend alerts** for now.
- **No recurring transactions.**
- **No receipt attachments, no CSV/PDF export** — in-app report viewing only.
- **No offline-first requirements** on the mobile client for now (no
  idempotency keys / client-generated IDs needed yet).

These are deliberate MVP cuts, not oversights — revisit only if the product
direction changes.

## Domain model

- **User** — email, password (bcrypt-hashed), fullName, role, createdAt.
  `Role` (USER/ADMIN) exists from initial scaffolding but has no real
  meaning yet in a single-user app; not building admin features around it.
- **Category** — belongs to a user, name (+ optionally icon/color later).
  User-owned and fully editable: create, rename, delete. Seeded with
  defaults on registration: Food, Entertainment, Education, Investment,
  Dressing, Bills, Transport, Health, Other.
- **Transaction** (expense) — belongs to a user and a category: amount,
  date, note/description, createdAt.

## MVP API surface

- **Auth** (done) — `POST /api/v1/auth/register`, `POST /api/v1/auth/login`
  → JWT bearer token. Stateless sessions, `SecurityConfig` requires a valid
  token on everything except `/api/v1/health` and `/api/v1/auth/**`.
- **Categories** — `GET/POST /api/v1/categories`,
  `PUT/DELETE /api/v1/categories/{id}`. Scoped to the authenticated user.
- **Transactions** — `GET/POST /api/v1/transactions`,
  `PUT/DELETE /api/v1/transactions/{id}`. `GET` supports filtering by date
  range and category.
- **Reports** — `GET /api/v1/reports/summary?from=&to=&groupBy=day|week|month|category`.
  Server-side aggregation (totals per bucket/category) so the mobile client
  stays dumb and report numbers stay consistent across views. Weekly/
  monthly/yearly are just callers passing the right `from`/`to`/`groupBy`.

## Tech stack

**Backend** (this repo): Java 21, Spring Boot 3.5, Spring Data JPA, Spring
Security (JWT via jjwt), Postgres 16 (via `docker-compose.yml`, port
55432), Lombok, springdoc-openapi for Swagger docs.

**Mobile** (`spendwise-mobile`, not yet created): React Native + TypeScript.

## Local dev

```
docker compose up -d          # start Postgres
./mvnw spring-boot:run        # start the API on :8080
```
Requires Docker Desktop running first — Hibernate needs a live DB
connection to detect its SQL dialect at startup, so `spring-boot:run`
fails fast if Postgres isn't reachable.

## Decisions log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-07-11 | JWT-based stateless auth over HTTP Basic | Mobile client needs token-based auth; no server-side session state to manage. |
| 2026-07-11 | Expense-only, single-currency, no accounts/budgets/recurring for MVP | Keep MVP scope tight; these are the most requested phase-2 features if the app gets traction. |
| 2026-07-11 | Categories are user-owned and editable, seeded with sensible defaults on registration | Avoids an empty-state cold-start while still letting users tailor categories to their spending. |
| 2026-07-11 | Reports are server-aggregated via one flexible summary endpoint, not raw transactions + client-side aggregation | Keeps report numbers consistent across app screens and avoids duplicating aggregation logic on the client. |
| 2026-07-11 | Mobile client is React Native + TypeScript in a separate repo (`spendwise-mobile`) | Cross-platform from one codebase; separate repo keeps toolchains/CI independent from the Spring Boot backend. |

## Out of scope (revisit later, not now)

Income tracking, multi-currency, accounts/wallets, budgets & alerts,
recurring transactions, shared/multi-user expenses, offline sync, receipt
attachments, CSV/PDF export.

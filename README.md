# advanced-online-bookstore

Backend for an online bookstore built with Spring Boot (Java 21). This repo contains:
- A **monolith API** (this project, port `8081`)
- A **payment microservice** in [`payment-service/`](payment-service) (port `8082`)

Both services are configured via environment variables (see [`.env.example`](.env.example)).

## Features

- Authentication and authorization:
  - JWT auth
  - Optional Google OAuth2 login
  - Role-based access for admin endpoints
- Books:
  - Public catalog endpoints
  - Admin CRUD + soft delete
  - Image upload (served under `/uploads/**`)
- Cart / orders / wishlist / reviews (see the Postman guides in this repo)
- Caching:
  - Spring Cache with optional Redis backend
- Payments:
  - Stripe PaymentIntent creation (payment-service)
  - Stripe webhook endpoint (payment-service)

## Tech Stack

- Java 21
- Spring Boot 4.x
- Spring Security + OAuth2 client
- Spring Data JPA (Postgres)
- SpringDoc OpenAPI (Swagger UI)
- Optional Redis (cache; and Spring Session is on the classpath)
- Docker / docker compose

## Quick Start (Docker Compose)

This is the recommended local setup: API + Postgres + Redis + payment-service + payment Postgres.

1. Create `.env` (git-ignored):

```powershell
Copy-Item .env.example .env
```

2. Edit `.env` and set at least:
- `JWT_SECRET` (required)
- `INTERNAL_API_TOKEN` (required if you run `payment-service` against the monolith)
- Optional: Stripe + Google OAuth credentials

3. Start:

```powershell
docker compose up --build
```

Useful URLs:
- API base: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

Stop:

```powershell
docker compose down
```

## Run Without Docker (Maven)

You need a Postgres database (and optionally Redis) available.

1. Set env vars (recommended: put them in your shell, not in git-tracked files):
- `DB_URL` (JDBC URL, for example `jdbc:postgresql://localhost:5432/bookstore_db`)
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `INTERNAL_API_TOKEN` (if payment-service calls the monolith)

2. Start the monolith:

```powershell
mvn spring-boot:run
```

3. Start payment-service (optional):

```powershell
cd payment-service
mvn spring-boot:run
```

## Configuration

The monolith reads config from [`src/main/resources/application.yml`](src/main/resources/application.yml).

Common environment variables:
- Server:
  - `PORT` (used by many PaaS providers)
  - `SERVER_PORT` (local override; default `8081`)
- Database:
  - `DB_URL` (required; JDBC URL)
  - `DB_USERNAME` (required)
  - `DB_PASSWORD` (optional default is empty)
- JPA:
  - `JPA_DDL_AUTO` (default `update`)
  - `JPA_SHOW_SQL` (default `false`)
- Cache/Redis:
  - `CACHE_TYPE` (`simple` or `redis`)
  - `REDIS_URL` (preferred when your provider supplies a single URL)
  - `REDIS_HOST`, `REDIS_PORT`
  - `CACHE_REDIS_TTL` (default `10m`)
- JWT:
  - `JWT_SECRET` (required)
  - `JWT_EXPIRATION` (default `86400000`)
- Internal service auth:
  - `INTERNAL_API_TOKEN` (recommended)
- Uploads:
  - `APP_UPLOAD_DIR` (default `uploads`)
- Spring Session (Redis):
  - `SESSION_STORE_TYPE` (default `none`)
    - If you enable OAuth2 and run multiple instances, consider setting `redis` and providing Redis.

Payment-service config is in
[`payment-service/src/main/resources/application.yml`](payment-service/src/main/resources/application.yml).

Payment-service important environment variables:
- `PORT` / `SERVER_PORT` (default `8082`)
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`
- `INTERNAL_API_TOKEN`
- `ORDER_SERVICE_BASE_URL` (default `http://localhost:8081`)
- Stripe:
  - `STRIPE_SECRET_KEY`
  - `STRIPE_WEBHOOK_SECRET`
  - `STRIPE_CURRENCY`

Stripe webhook endpoint:
- `POST /api/payments/stripe/webhook`

## Uploads

Images are served from `/uploads/**` and stored on disk in `APP_UPLOAD_DIR` (default `uploads/`).
On most cloud platforms the filesystem is **ephemeral**, so use a persistent disk or move uploads to object storage.

## Testing and API Guides

This repo includes step-by-step docs:
- [`DOCKER_GUIDE.md`](DOCKER_GUIDE.md)
- [`TESTING_GUIDE.md`](TESTING_GUIDE.md)
- [`CACHING_REDIS_GUIDE.md`](CACHING_REDIS_GUIDE.md)
- `*_POSTMAN_GUIDE.md` files (endpoint coverage and examples)
- Stripe setup: [`STRIPE_PAYMENT_TESTING_GUIDE.md`](STRIPE_PAYMENT_TESTING_GUIDE.md)

## Security Notes

- Do not commit secrets (DB passwords, OAuth credentials, Stripe keys, JWT secrets).
- Prefer environment variables or secret managers for production deployments.


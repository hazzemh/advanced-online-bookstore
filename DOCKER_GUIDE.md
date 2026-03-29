# Docker Guide (API + Postgres + Redis)

This repo includes a Docker setup for local dev that runs:
- Spring Boot API (port `8081`)
- Postgres (port `5432`)
- Redis (port `6379`) for caching

## Start

```bash
docker compose up --build
```

Stop:

```bash
docker compose down
```

Stop and remove volumes (deletes Postgres data):

```bash
docker compose down -v
```

## Useful URLs

- API base: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## Configuration

All runtime config is passed via environment variables in `docker-compose.yml` and mapped to the placeholders in `application.example.yml`.

Important vars:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `CACHE_TYPE` (set to `redis`)
- `REDIS_HOST`, `REDIS_PORT`
- `JWT_SECRET` (must be long enough for HS256)

Optional integrations:
- Stripe: `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`, `STRIPE_CURRENCY`
- Google OAuth: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`


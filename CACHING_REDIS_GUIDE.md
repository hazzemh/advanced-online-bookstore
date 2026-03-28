# Caching + Redis Guide

This project uses Spring Cache annotations (`@Cacheable`, `@CacheEvict`) to cache common read operations.

## Why Redis?

- In-memory caches (like `simple` or Caffeine) are fastest but only work inside a single app instance.
- Redis is shared and works across multiple app instances, and survives app restarts.

## Run Redis Locally (Docker)

```bash
docker run --name bookstore-redis -p 6379:6379 redis
```

If you already have a container with that name:

```bash
docker start bookstore-redis
```

## Enable Redis Cache (Local `application.yml`)

Your repo-root `application.yml` is ignored by git, so you can safely set local runtime config there.

```yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 10m
  data:
    redis:
      host: localhost
      port: 6379
```

If Redis is not running, set `spring.cache.type: simple` (or remove the `spring.cache` block) to fall back to in-memory caching.

## What Is Cached

- Book details:
  - `BookService.getBookById(bookId)` -> cache `bookById`
- Book lists/search:
  - `getAllBooks`, `findByGenre`, `searchByTitle`, `searchByAuthor`, `findTopRatedBooks`, `findAvailableBooks`
- Categories:
  - `GET /api/books/genres` -> cache `genres` (distinct active genres)
- Recommendations:
  - `RecommendationService.recommend(...)` -> cache `recommendations`
  - `RecommendationService.getUserPreferences(...)` -> cache `recommendationProfile`

Cache invalidation
- Any book create/update/delete/image change evicts the related list caches and the book details cache.
- Order events clear recommendation caches (coarse eviction) to keep results reasonably fresh.


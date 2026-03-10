# Wishlist Testing Guide

## Base URL
`http://localhost:8081`

## Authentication
All wishlist endpoints require a valid JWT token.

Header:
`Authorization: Bearer <your-token>`

## Endpoints

### 1) Add Book To Wishlist
- Method: `POST`
- URL: `/api/wishlist/books/{bookId}`

Example:
`POST http://localhost:8081/api/wishlist/books/7f3b77cc-7fa8-4f13-80c3-26f31b2bf7f4`

Expected:
- `201 Created`
- Response contains wishlist item + book details.

### 2) Get Current User Wishlist
- Method: `GET`
- URL: `/api/wishlist?page=0&size=10`

Example:
`GET http://localhost:8081/api/wishlist?page=0&size=10`

Expected:
- `200 OK`
- Paged wishlist items for authenticated user only.

### 3) Check If Book Is In Wishlist
- Method: `GET`
- URL: `/api/wishlist/books/{bookId}/exists`

Example:
`GET http://localhost:8081/api/wishlist/books/7f3b77cc-7fa8-4f13-80c3-26f31b2bf7f4/exists`

Expected:
- `200 OK`
- Body: `true` or `false`.

### 4) Remove Book From Wishlist
- Method: `DELETE`
- URL: `/api/wishlist/books/{bookId}`

Example:
`DELETE http://localhost:8081/api/wishlist/books/7f3b77cc-7fa8-4f13-80c3-26f31b2bf7f4`

Expected:
- `204 No Content`

## Validation Cases
1. Add same book twice for same user -> should fail.
2. Add book for user A then check user B -> user B should not see it.
3. Remove non-existing wishlist item -> should fail.
4. Add inactive book -> should fail.

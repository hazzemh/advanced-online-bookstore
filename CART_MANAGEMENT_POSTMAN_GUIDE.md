## Cart Management API - Postman Testing Guide

This guide explains how to test Cart endpoints using Postman.

Base URL: `http://localhost:8081`

All cart endpoints require a valid JWT token.

Header:
`Authorization: Bearer <your-token>`

---

## 1) Get Current User Cart

Method: `GET`

URL: `/api/cart`

Example:
`GET http://localhost:8081/api/cart`

Expected:
- `200 OK`
- Returns `CartResponse` with items, subtotal, totalQuantity.

---

## 2) Add Book To Cart

Method: `POST`

URL: `/api/cart/items`

Body (JSON):
```json
{
  "bookId": "7f3b77cc-7fa8-4f13-80c3-26f31b2bf7f4",
  "quantity": 2
}
```

Expected:
- `201 Created`
- Adds item or increments quantity if the same book is already in the cart.

---

## 3) Update Cart Item Quantity

Method: `PUT`

URL: `/api/cart/items/{itemId}`

Body (JSON):
```json
{
  "quantity": 3
}
```

Expected:
- `200 OK`
- If `quantity <= 0`, the item is removed.

---

## 4) Remove Cart Item

Method: `DELETE`

URL: `/api/cart/items/{itemId}`

Expected:
- `200 OK`
- Returns updated cart.

---

## 5) Clear Cart

Method: `DELETE`

URL: `/api/cart`

Expected:
- `204 No Content`

---

## Validation Cases

1. Add with `quantity <= 0` should fail with `400`.
2. Add more than available stock should fail with `400`.
3. Add inactive book should fail.
4. Update/remove an item that belongs to another user should fail.


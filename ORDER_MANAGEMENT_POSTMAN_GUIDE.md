## Order Management API - Postman Testing Guide

Base URL: `http://localhost:8081`

User endpoints require JWT:
`Authorization: Bearer <your-token>`

Admin endpoints require an admin JWT (role ADMIN).

---

## User Endpoints

### 1) Create Order

Method: `POST`

URL: `/api/orders`

Body (JSON):
```json
{
  "items": [
    { "bookId": "7f3b77cc-7fa8-4f13-80c3-26f31b2bf7f4", "quantity": 2 },
    { "bookId": "0b2f2c2b-19f8-4f33-9b51-2a2bbd1a9a33", "quantity": 1 }
  ]
}
```

Expected:
- `201 Created`
- Returns created order with `status`, `subtotal`, and items.

---

### 2) Get My Orders (History)

Method: `GET`

URL: `/api/orders?page=0&size=10`

Expected:
- `200 OK`
- Paged order history for the authenticated user.

---

### 3) Get My Order Details

Method: `GET`

URL: `/api/orders/{orderId}`

Expected:
- `200 OK`

---

### 4) Cancel My Order

Method: `POST`

URL: `/api/orders/{orderId}/cancel`

Expected:
- `200 OK`
- Sets order to `CANCELED` (not allowed after `SHIPPED` or `DELIVERED`).

---

## Admin Endpoints

### 1) Get All Orders

Method: `GET`

URL: `/api/admin/orders?page=0&size=10`

Optional filter:
`/api/admin/orders?status=PROCESSING`

Expected:
- `200 OK`

---

### 2) Update Order Status

Method: `PUT`

URL: `/api/admin/orders/{orderId}/status`

Body (JSON):
```json
{ "status": "SHIPPED" }
```

Expected:
- `200 OK`
- Updates tracking timestamps (`shippedAt`, `deliveredAt`, `canceledAt`) where applicable.


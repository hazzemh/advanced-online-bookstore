## 📚 Book Management API - Postman Testing Guide

This guide explains how to test all Book Management endpoints using Postman.

---

## 🚀 Prerequisites

1. **Postman** installed (https://www.postman.com/downloads/)
2. **Application running** on `http://localhost:8080`
3. **Admin user registered** with ADMIN role (for admin endpoints)
4. **JWT token** from authentication (see below)

---

## 🔐 Step 1: Get Authentication Token

### Default Admin User (Auto-Created)

When the application starts for the first time, a default admin user is automatically created:

- **Email:** `admin@bookstore.com`
- **Password:** `AdminPassword123!`
- **Role:** ADMIN

**⚠️ IMPORTANT:** Change this password after first login for security!

### Login as Admin
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@bookstore.com",
  "password": "AdminPassword123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBib29rc3RvcmUuY29tIiwiaWF0IjoxNzQwNzI4MDAwLCJleHAiOjE3NDA4MTQ0MDB9.xxx"
}
```

### Register Additional Users (Optional)
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "UserPassword123!",
  "firstName": "Regular",
  "lastName": "User"
}
```

**Note:** Regular users get USER role by default. Only the auto-created admin has ADMIN role.

---

## ✅ ADMIN ENDPOINTS - Book Management

### **1. CREATE A NEW BOOK**

**URL:** `POST http://localhost:8080/api/admin/books`

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "description": "A classic American novel about the Jazz Age",
  "price": 12.99,
  "stockQuantity": 50,
  "isbn": "978-0-7432-7356-5",
  "genre": "Fiction",
  "publicationYear": 1925,
  "pages": 180,
  "publisher": "Scribner"
}
```

**Expected Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "description": "A classic American novel about the Jazz Age",
  "price": 12.99,
  "stockQuantity": 50,
  "isbn": "978-0-7432-7356-5",
  "genre": "Fiction",
  "publicationYear": 1925,
  "pages": 180,
  "publisher": "Scribner",
  "imageUrl": null,
  "averageRating": 0.0,
  "totalReviews": 0,
  "isActive": true
}
```

**Test Points:**
- ✅ Status should be 201 CREATED
- ✅ Book ID should be generated
- ✅ isActive should be true by default
- ✅ averageRating should be 0.0 initially

---

### **2. UPDATE AN EXISTING BOOK**

**URL:** `PUT http://localhost:8080/api/admin/books/{{bookId}}`

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "The Great Gatsby - Special Edition",
  "author": "F. Scott Fitzgerald",
  "description": "A classic American novel - Updated description",
  "price": 14.99,
  "stockQuantity": 75,
  "isbn": "978-0-7432-7356-5",
  "genre": "Fiction",
  "publicationYear": 1925,
  "pages": 180,
  "publisher": "Scribner"
}
```

**Expected Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "The Great Gatsby - Special Edition",
  "price": 14.99,
  "stockQuantity": 75,
  // ... other fields
}
```

**Test Points:**
- ✅ Status should be 200 OK
- ✅ Title and price should be updated
- ✅ Stock quantity should increase to 75

---

### **3. DELETE A BOOK (Soft Delete)**

**URL:** `DELETE http://localhost:8080/api/admin/books/{{bookId}}`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response (204 No Content):**
- No response body, just status 204

**Test Points:**
- ✅ Status should be 204 NO CONTENT
- ✅ Book is marked inactive (soft delete)
- ✅ Book won't appear in public listing

---

### **4. LIST ALL BOOKS (Admin)**

**URL:** `GET http://localhost:8080/api/admin/books?page=0&size=10`

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "The Great Gatsby",
      "author": "F. Scott Fitzgerald",
      // ... other fields
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": true,
    "sorted": false,
    "unsorted": true
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

**Test Points:**
- ✅ Status should be 200 OK
- ✅ Content array contains books
- ✅ Pagination info is included
- ✅ totalElements shows count

---

## 📖 PUBLIC ENDPOINTS - Book Browsing

### **5. GET SINGLE BOOK BY ID**

**URL:** `GET http://localhost:8080/api/books/{{bookId}}`

**Headers:**
```
Content-Type: application/json
```

**Note:** No authentication required!

**Expected Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "description": "A classic American novel",
  "price": 12.99,
  "stockQuantity": 50,
  "isbn": "978-0-7432-7356-5",
  "genre": "Fiction",
  "publicationYear": 1925,
  "pages": 180,
  "publisher": "Scribner",
  "imageUrl": null,
  "averageRating": 0.0,
  "totalReviews": 0,
  "isActive": true
}
```

---

### **6. GET ALL BOOKS (Public Listing)**

**URL:** `GET http://localhost:8080/api/books?page=0&size=10`

**Headers:**
```
Content-Type: application/json
```

**Query Parameters:**
- `page` (default: 0) - Page number (0-based)
- `size` (default: 10) - Items per page

**Expected Response (200 OK):**
```json
{
  "content": [
    { /* book object */ },
    { /* book object */ }
  ],
  "pageable": { /* pagination info */ },
  "totalPages": 5,
  "totalElements": 45,
  "last": false,
  "size": 10,
  "number": 0,
  "numberOfElements": 10,
  "first": true,
  "empty": false
}
```

**Test Points:**
- ✅ Status should be 200 OK
- ✅ Only active books shown (isActive = true)
- ✅ Pagination works correctly
- ✅ No authentication required

---

### **7. SEARCH BY TITLE**

**URL:** `GET http://localhost:8080/api/books/search/title?keyword=Gatsby&page=0&size=10`

**Headers:**
```
Content-Type: application/json
```

**Query Parameters:**
- `keyword` - Search term (case-insensitive)
- `page` - Page number
- `size` - Items per page

**Example Requests:**
```
# Search for "Gatsby"
GET http://localhost:8080/api/books/search/title?keyword=Gatsby&page=0&size=10

# Search for "great" (case-insensitive)
GET http://localhost:8080/api/books/search/title?keyword=great&page=0&size=10
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "title": "The Great Gatsby",
      // ... other fields
    }
  ],
  "totalElements": 1,
  // ... pagination info
}
```

**Test Points:**
- ✅ Search is case-insensitive
- ✅ Partial matches work (% wildcard)
- ✅ Empty results return empty content array

---

### **8. SEARCH BY AUTHOR**

**URL:** `GET http://localhost:8080/api/books/search/author?keyword=Fitzgerald&page=0&size=10`

**Headers:**
```
Content-Type: application/json
```

**Example Request:**
```
GET http://localhost:8080/api/books/search/author?keyword=Fitzgerald
GET http://localhost:8080/api/books/search/author?keyword=Austen&page=0&size=5
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "author": "F. Scott Fitzgerald",
      // ... other fields
    }
  ]
}
```

---

### **9. FILTER BY GENRE**

**URL:** `GET http://localhost:8080/api/books/genre/Fiction?page=0&size=10`

**Headers:**
```
Content-Type: application/json
```

**Example Requests:**
```
GET http://localhost:8080/api/books/genre/Fiction
GET http://localhost:8080/api/books/genre/Mystery?page=0&size=20
GET http://localhost:8080/api/books/genre/Romance
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "genre": "Fiction",
      // ... other fields
    }
  ],
  "totalElements": 12
}
```

**Test Points:**
- ✅ Genre filtering works
- ✅ Case-sensitive genre matching
- ✅ Returns empty if no books in genre

---

### **10. FILTER BY PRICE RANGE**

**URL:** `GET http://localhost:8080/api/books/price-range?minPrice=10.00&maxPrice=20.00&page=0&size=10`

**Headers:**
```
Content-Type: application/json
```

**Query Parameters:**
- `minPrice` - Minimum price (required)
- `maxPrice` - Maximum price (required)
- `page` - Page number
- `size` - Items per page

**Example Requests:**
```
# Books between $10 and $20
GET http://localhost:8080/api/books/price-range?minPrice=10.00&maxPrice=20.00

# Cheap books under $15
GET http://localhost:8080/api/books/price-range?minPrice=0&maxPrice=15.00&page=0&size=20

# Expensive books over $30
GET http://localhost:8080/api/books/price-range?minPrice=30.00&maxPrice=999.99
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "price": 12.99,
      // ... other fields
    },
    {
      "price": 15.50,
      // ... other fields
    }
  ],
  "totalElements": 5
}
```

**Test Points:**
- ✅ Price filtering is inclusive (between min and max)
- ✅ Decimal values work
- ✅ Results sorted by price

---

### **11. GET TOP-RATED BOOKS**

**URL:** `GET http://localhost:8080/api/books/top-rated?page=0&size=10`

**Headers:**
```
Content-Type: application/json
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "title": "The Great Gatsby",
      "averageRating": 4.8,
      // ... other fields
    },
    {
      "title": "To Kill a Mockingbird",
      "averageRating": 4.7,
      // ... other fields
    }
  ],
  "totalElements": 25
}
```

**Test Points:**
- ✅ Books sorted by averageRating descending
- ✅ Higher rated books appear first
- ✅ Only active books with ratings > 0

---

### **12. GET AVAILABLE BOOKS (In Stock)**

**URL:** `GET http://localhost:8080/api/books/available?page=0&size=10`

**Headers:**
```
Content-Type: application/json
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "title": "The Great Gatsby",
      "stockQuantity": 50,
      // ... other fields
    }
  ],
  "totalElements": 38
}
```

**Test Points:**
- ✅ Only books with stockQuantity > 0 returned
- ✅ Books with 0 stock are excluded
- ✅ Perfect for "In Stock" filters

---

## ⚠️ ERROR SCENARIOS & TESTING

### **Book Not Found**
```
GET http://localhost:8080/api/books/invalid-uuid-here
```

**Expected Response (500 Internal Server Error):**
```json
{
  "message": "Book not found",
  "status": 500,
  "timestamp": 1740728000000
}
```

---

### **Duplicate ISBN**
Try creating 2 books with same ISBN:

```
POST http://localhost:8080/api/admin/books
{
  "isbn": "978-0-7432-7356-5",
  // ... other fields same
}
```

**Expected Response (500 Internal Server Error):**
```json
{
  "message": "Book with ISBN 978-0-7432-7356-5 already exists",
  "status": 500
}
```

---

### **Unauthorized Access (No Token)**
```
POST http://localhost:8080/api/admin/books
{
  // ... book data
}
```

**Expected Response (401 Unauthorized):**
```json
{
  "error": "Unauthorized"
}
```

---

### **Forbidden (User without ADMIN role)**
If logged in as regular user trying admin endpoint:

```
POST http://localhost:8080/api/admin/books
Authorization: Bearer {{userToken}}
```

**Expected Response (403 Forbidden):**
```
No content, just 403 status
```

---

## 🧪 TEST WORKFLOW

### **Complete Testing Sequence:**

1. **Register/Login** and get token
2. **Create Books** (Test #1) - Create 5-10 different books
3. **Update Book** (Test #2) - Update price/stock
4. **Test Public Search** (Tests #5-12):
   - Search by title
   - Search by author
   - Filter by genre
   - Filter by price
   - Get top-rated
   - Get available
5. **Test Pagination** - Try different page sizes
6. **Delete Book** (Test #3) - Soft delete a book
7. **Verify Deletion** - Should not appear in public listing
8. **Test Error Cases** - Try unauthorized access

---

## 💡 POSTMAN TIPS

### **Save Token to Environment Variable**
After login, in response, go to **Tests** tab:
```javascript
var jsonData = pm.response.json();
pm.environment.set("token", jsonData.token);
```

Then use `{{token}}` in Authorization header.

### **Pagination Testing**
```
# Page 1 (items 0-9)
?page=0&size=10

# Page 2 (items 10-19)
?page=1&size=10

# Page 3 with larger size
?page=2&size=20
```

### **Quick Test Multiple Books**
Create requests with different search terms:
- Title: "Great", "Gatsby", "Kill"
- Author: "Fitzgerald", "Lee", "Austen"
- Genre: "Fiction", "Mystery"
- Price: 0-10, 10-20, 20+

---

## ✅ Checklist

Before considering testing complete:
- [ ] Can create books (admin only)
- [ ] Can update book details
- [ ] Can soft delete books
- [ ] Can search by title
- [ ] Can search by author
- [ ] Can filter by genre
- [ ] Can filter by price range
- [ ] Can get top-rated books
- [ ] Can get available books
- [ ] Pagination works correctly
- [ ] Unauthorized requests return 401
- [ ] Non-admin users can't access /api/admin/**
- [ ] Public endpoints don't require authentication

---

## 🎯 Expected Test Results Summary

| Endpoint | Method | Auth | Expected Status |
|----------|--------|------|-----------------|
| /api/admin/books | POST | ADMIN | 201 Created |
| /api/admin/books/{id} | PUT | ADMIN | 200 OK |
| /api/admin/books/{id} | DELETE | ADMIN | 204 No Content |
| /api/admin/books | GET | ADMIN | 200 OK |
| /api/books | GET | None | 200 OK |
| /api/books/{id} | GET | None | 200 OK |
| /api/books/search/title | GET | None | 200 OK |
| /api/books/search/author | GET | None | 200 OK |
| /api/books/genre/{genre} | GET | None | 200 OK |
| /api/books/price-range | GET | None | 200 OK |
| /api/books/top-rated | GET | None | 200 OK |
| /api/books/available | GET | None | 200 OK |

---

**Happy Testing! 🚀**

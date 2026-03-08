# Review & Rating Feature - Postman Testing Guide

## Overview
This guide demonstrates how to test the User Reviews and Ratings endpoints using Postman.

## Prerequisites
- Application running on `http://localhost:8080`
- Valid JWT token from authentication
- A book ID to review
- User ID for testing

## Setup Instructions

1. **Get Authentication Token**
   - First, register and login to get a JWT token
   - Use this token in the `Authorization` header for all requests

2. **Create Books (if needed)**
   - Admin users can create books before testing reviews

## API Endpoints

### 1. Create a Review
**Endpoint:** `POST /api/reviews`
**Auth Required:** Yes (USER role)

**Request Headers:**
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Body:**
```json
{
  "bookId": "550e8400-e29b-41d4-a716-446655440000",
  "rating": 5,
  "comment": "This is an amazing book! Highly recommended."
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "bookId": "550e8400-e29b-41d4-a716-446655440000",
  "bookTitle": "The Great Gatsby",
  "userId": "550e8400-e29b-41d4-a716-446655440002",
  "userName": "John Doe",
  "rating": 5,
  "comment": "This is an amazing book! Highly recommended.",
  "isVerifiedPurchase": false,
  "createdAt": "2026-03-08T10:30:00",
  "updatedAt": "2026-03-08T10:30:00"
}
```

**Notes:**
- Rating must be between 1 and 5
- A user can only review each book once
- Comment is optional

### 2. Get All Reviews for a Book
**Endpoint:** `GET /api/reviews/book/{bookId}`
**Auth Required:** No

**Query Parameters:**
- `page` (default: 0) - Page number for pagination
- `size` (default: 10) - Page size

**Example:**
```
GET /api/reviews/book/550e8400-e29b-41d4-a716-446655440000?page=0&size=5
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "bookId": "550e8400-e29b-41d4-a716-446655440000",
      "bookTitle": "The Great Gatsby",
      "userId": "550e8400-e29b-41d4-a716-446655440002",
      "userName": "John Doe",
      "rating": 5,
      "comment": "This is an amazing book!",
      "isVerifiedPurchase": false,
      "createdAt": "2026-03-08T10:30:00",
      "updatedAt": "2026-03-08T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5,
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
  "size": 5,
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

### 3. Get Reviews with Minimum Rating Filter
**Endpoint:** `GET /api/reviews/book/{bookId}/min-rating`
**Auth Required:** No

**Query Parameters:**
- `minRating` (required) - Minimum rating (1-5)
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Example:**
```
GET /api/reviews/book/550e8400-e29b-41d4-a716-446655440000/min-rating?minRating=4&page=0&size=10
```

### 4. Get All Reviews by a User
**Endpoint:** `GET /api/reviews/user/{userId}`
**Auth Required:** No

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Example:**
```
GET /api/reviews/user/550e8400-e29b-41d4-a716-446655440002?page=0&size=10
```

### 5. Get a Specific Review
**Endpoint:** `GET /api/reviews/{reviewId}`
**Auth Required:** No

**Example:**
```
GET /api/reviews/550e8400-e29b-41d4-a716-446655440001
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "bookId": "550e8400-e29b-41d4-a716-446655440000",
  "bookTitle": "The Great Gatsby",
  "userId": "550e8400-e29b-41d4-a716-446655440002",
  "userName": "John Doe",
  "rating": 5,
  "comment": "This is an amazing book!",
  "isVerifiedPurchase": false,
  "createdAt": "2026-03-08T10:30:00",
  "updatedAt": "2026-03-08T10:30:00"
}
```

### 6. Check if User Has Reviewed a Book
**Endpoint:** `GET /api/reviews/book/{bookId}/user/{userId}/has-review`
**Auth Required:** No

**Example:**
```
GET /api/reviews/book/550e8400-e29b-41d4-a716-446655440000/user/550e8400-e29b-41d4-a716-446655440002/has-review
```

**Response (200 OK):**
```json
true
```

### 7. Get User's Review for a Book
**Endpoint:** `GET /api/reviews/book/{bookId}/user/{userId}`
**Auth Required:** No

**Example:**
```
GET /api/reviews/book/550e8400-e29b-41d4-a716-446655440000/user/550e8400-e29b-41d4-a716-446655440002
```

### 8. Update a Review
**Endpoint:** `PUT /api/reviews/{reviewId}`
**Auth Required:** Yes (USER role)

**Request Headers:**
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Body:**
```json
{
  "rating": 4,
  "comment": "Updated comment - still a great book, but not perfect."
}
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "bookId": "550e8400-e29b-41d4-a716-446655440000",
  "bookTitle": "The Great Gatsby",
  "userId": "550e8400-e29b-41d4-a716-446655440002",
  "userName": "John Doe",
  "rating": 4,
  "comment": "Updated comment - still a great book, but not perfect.",
  "isVerifiedPurchase": false,
  "createdAt": "2026-03-08T10:30:00",
  "updatedAt": "2026-03-08T10:35:00"
}
```

**Notes:**
- Only the review author can update their review
- Updating a review will recalculate the book's average rating

### 9. Delete a Review
**Endpoint:** `DELETE /api/reviews/{reviewId}`
**Auth Required:** Yes (USER role)

**Request Headers:**
```
Authorization: Bearer {JWT_TOKEN}
```

**Response (204 No Content)**

**Notes:**
- Only the review author can delete their review
- Deleting a review will recalculate the book's average rating

## Postman Collection Setup

### Environment Variables
Create a Postman environment with these variables:
```
BASE_URL = http://localhost:8080
JWT_TOKEN = {your_jwt_token_here}
BOOK_ID = {book_uuid_here}
USER_ID = {user_uuid_here}
REVIEW_ID = {review_uuid_here}
```

### Collection Variables
In your request URLs, use:
```
{{BASE_URL}}/api/reviews/book/{{BOOK_ID}}
{{BASE_URL}}/api/reviews/{{REVIEW_ID}}
{{BASE_URL}}/api/reviews/user/{{USER_ID}}
```

### Authentication
Set the Authorization tab to "Bearer Token" and use:
```
{{JWT_TOKEN}}
```

## Testing Scenarios

### Scenario 1: Create and View a Review
1. Create a new review with rating 5
2. Get all reviews for the book
3. Verify the review appears in the list
4. Check the book's average rating has been updated

### Scenario 2: Filter by Rating
1. Create multiple reviews with different ratings
2. Filter by minimum rating 4
3. Verify only reviews with 4+ stars appear

### Scenario 3: Update and Delete
1. Create a review
2. Update the review with a new comment
3. Verify the `updatedAt` timestamp changed
4. Delete the review
5. Verify it's no longer in the book's review list

### Scenario 4: Duplicate Review Validation
1. Create a review for a book by a user
2. Try to create another review for the same book by the same user
3. Expect a 400/409 error response

### Scenario 5: Authorization Check
1. Try to update a review you didn't create
2. Try to delete a review you didn't create
3. Expect 403 Forbidden responses

## Error Handling

### Validation Errors
- **Invalid rating (not 1-5):** Returns 400 Bad Request
- **User already reviewed book:** Returns 400/409 Conflict
- **Book not found:** Returns 404 Not Found
- **User not found:** Returns 404 Not Found

### Authorization Errors
- **No JWT token:** Returns 401 Unauthorized
- **Attempting to modify someone else's review:** Returns 403 Forbidden
- **User doesn't have required role:** Returns 403 Forbidden

## Key Concepts

### Pagination
- Reviews are paginated with configurable page size
- Use `page` and `size` query parameters
- Useful for displaying reviews in a scrollable list

### Average Rating Calculation
- When a review is created, updated, or deleted, the book's average rating is automatically recalculated
- Uses the formula: `AVG(all_ratings_for_book)`
- Rounded to one decimal place in responses

### Verified Purchase Badge
- Currently set to `false` for all reviews
- Will be `true` when integrated with Order/Payment system
- Indicates the reviewer actually purchased the book

## Tips for Testing

1. **Use Postman's Pre-request Script** to automatically update variables:
   ```javascript
   // After creating a review, extract the ID
   pm.environment.set("REVIEW_ID", pm.response.json().id);
   ```

2. **Test pagination** by creating multiple reviews and using different page numbers

3. **Monitor the book's average rating** as you create/update/delete reviews

4. **Test error scenarios** by:
   - Using invalid UUIDs
   - Providing invalid ratings
   - Attempting unauthorized actions


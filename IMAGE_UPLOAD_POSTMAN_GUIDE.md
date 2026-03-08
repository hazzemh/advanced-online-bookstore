# 📸 Image Upload Handling - Postman Testing Guide

This guide explains how to test image upload/download endpoints using Postman.

---

## 🚀 Prerequisites

1. **Postman** installed (https://www.postman.com/downloads/)
2. **Application running** on `http://localhost:8080`
3. **Admin JWT token** for authentication (from `/api/auth/login`)
4. **Existing book** (create one first via POST `/api/admin/books`)
5. **Image file** to upload (JPEG, PNG, GIF, or WebP)

---

## 📸 Image Upload Endpoints

### **1. Upload Book Image**

**URL:** `POST http://localhost:8080/api/admin/books/{bookId}/upload-image`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Body:** Form-data
```
Key: image
Value: [Select your image file]
Type: File
```

**Supported Image Formats:**
- JPEG/JPG
- PNG
- GIF
- WebP

**File Size Limit:** 5MB

**Example Request in Postman:**
1. Select **POST** method
2. Enter URL: `http://localhost:8080/api/admin/books/550e8400-e29b-41d4-a716-446655440000/upload-image`
3. Go to **Headers** tab
4. Add: `Authorization: Bearer {{token}}`
5. Go to **Body** tab
6. Select **form-data**
7. Add key `image` with type **File**
8. Click on the value field and select an image from your computer
9. Click **Send**

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
  "imageUrl": "/uploads/8dd314b9-84c1-4b8c-8733-76c73b9d40e2.jpg",
  "averageRating": 0.0,
  "totalReviews": 0,
  "isActive": true
}
```

**Test Points:**
- ✅ Status should be 200 OK
- ✅ imageUrl should be set (e.g., `/uploads/filename.jpg`)
- ✅ Image file should be saved in `uploads/` directory
- ✅ Previous image should be deleted if replaced

---

### **2. View Uploaded Image**

**URL:** `GET http://localhost:8080/uploads/{fileName}`

**Headers:**
```
No authentication required!
```

**Example:**
```
GET http://localhost:8080/uploads/8dd314b9-84c1-4b8c-8733-76c73b9d40e2.jpg
```

**Expected Response:** 
- Image file directly (200 OK)
- Content-Type: `image/jpeg` (or appropriate image type)

**Test Points:**
- ✅ Can directly access image via browser or Postman
- ✅ No authentication needed
- ✅ Image displays correctly

---

### **3. Delete Book Image**

**URL:** `DELETE http://localhost:8080/api/admin/books/{bookId}/image`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Expected Response (204 No Content):**
- No response body

**Test Points:**
- ✅ Status should be 204 NO CONTENT
- ✅ Image file deleted from server
- ✅ imageUrl in book set to null
- ✅ imagePath in book set to null

---

## ⚠️ Error Scenarios & Testing

### **Empty File**
```
POST http://localhost:8080/api/admin/books/{bookId}/upload-image

Body: form-data
image: [empty file]
```

**Expected Response (400 Bad Request):**
```json
{
  "message": "File cannot be empty",
  "status": 400,
  "timestamp": 1741441200000
}
```

---

### **File Size Exceeds 5MB**
```
POST http://localhost:8080/api/admin/books/{bookId}/upload-image

Body: form-data
image: [file larger than 5MB]
```

**Expected Response (400 Bad Request):**
```json
{
  "message": "File size exceeds maximum limit of 5MB",
  "status": 400,
  "timestamp": 1741441200000
}
```

---

### **Invalid File Type (PDF, Video, etc.)**
```
POST http://localhost:8080/api/admin/books/{bookId}/upload-image

Body: form-data
image: document.pdf
```

**Expected Response (400 Bad Request):**
```json
{
  "message": "File type not allowed. Allowed types: JPEG, PNG, GIF, WebP",
  "status": 400,
  "timestamp": 1741441200000
}
```

---

### **Book Not Found**
```
DELETE http://localhost:8080/api/admin/books/invalid-uuid/image
Authorization: Bearer {{token}}
```

**Expected Response (500 Internal Server Error):**
```json
{
  "message": "Book not found",
  "status": 500,
  "timestamp": 1741441200000
}
```

---

### **Unauthorized Access (No Token)**
```
POST http://localhost:8080/api/admin/books/{bookId}/upload-image
[No Authorization header]
```

**Expected Response (401 Unauthorized):**
```json
{
  "error": "Unauthorized"
}
```

---

### **Forbidden (Regular User Without Admin Role)**
```
POST http://localhost:8080/api/admin/books/{bookId}/upload-image
Authorization: Bearer {{userToken}}
```

**Expected Response (403 Forbidden):**
- No content, just 403 status

---

## 🧪 Complete Testing Workflow

### **Step 1: Create or Get a Book ID**
```
GET http://localhost:8080/api/books?page=0&size=10
```
Copy the `id` from response (or create a new book first)

### **Step 2: Upload Image to Book**
```
POST http://localhost:8080/api/admin/books/{{bookId}}/upload-image

Headers:
Authorization: Bearer {{token}}

Body: form-data
image: [select your image file]
```

### **Step 3: Save Image URL from Response**
Note the `imageUrl` from response, e.g., `/uploads/8dd314b9-84c1-4b8c-8733-76c73b9d40e2.jpg`

### **Step 4: View the Uploaded Image**
```
GET http://localhost:8080/uploads/8dd314b9-84c1-4b8c-8733-76c73b9d40e2.jpg
```
Image should display in Postman or browser

### **Step 5: Get Book and Verify Image URL**
```
GET http://localhost:8080/api/books/{{bookId}}
```
Verify `imageUrl` is populated

### **Step 6: Upload New Image (Replace Previous)**
```
POST http://localhost:8080/api/admin/books/{{bookId}}/upload-image
[select different image]
```
Verify old image was deleted and new one is saved

### **Step 7: Delete Image**
```
DELETE http://localhost:8080/api/admin/books/{{bookId}}/image
Authorization: Bearer {{token}}
```
Status should be 204

### **Step 8: Verify Image Removed**
```
GET http://localhost:8080/api/books/{{bookId}}
```
Verify `imageUrl` is now null

---

## 💡 Postman Tips

### **Save Book ID as Variable**
After creating/fetching a book:
1. Go to response JSON
2. Highlight the `id` value
3. Right-click → Set: {{bookId}}

### **Test Different Image Formats**
Upload various image types to test:
- test-image.jpg (JPEG)
- test-image.png (PNG)
- test-image.gif (GIF)
- test-image.webp (WebP)

### **Create Test Images**
Use online tools to create test images:
- https://www.photopea.com/ (free photo editor)
- Generate small test images
- Keep file sizes under 5MB

### **File Upload Tips in Postman**
1. Go to **Body** tab
2. Select **form-data**
3. Key: `image`
4. Type: **File** (dropdown)
5. Value: Click and select file from computer

---

## 📁 Upload Directory Structure

Files are stored in:
```
project-root/
└── uploads/
    ├── 8dd314b9-84c1-4b8c-8733-76c73b9d40e2.jpg
    ├── a1f2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d.png
    └── ... [other uploaded images]
```

---

## 🔒 Security Features

**File Validation:**
- ✅ File type validation (JPEG, PNG, GIF, WebP only)
- ✅ File size limit (5MB max)
- ✅ Empty file detection
- ✅ Path traversal prevention (e.g., `../../../etc/passwd`)
- ✅ Unique filename generation (UUID-based)

**Access Control:**
- ✅ Upload/Delete: ADMIN role only
- ✅ View: Public (no authentication needed)
- ✅ Cannot access files outside upload directory

---

## ✅ Complete Test Checklist

- [ ] Can upload JPEG image
- [ ] Can upload PNG image
- [ ] Can upload GIF image
- [ ] Can upload WebP image
- [ ] Cannot upload PDF file
- [ ] Cannot upload empty file
- [ ] Cannot upload file > 5MB
- [ ] Can view uploaded image via URL
- [ ] Can replace image (old one deleted)
- [ ] Can delete image
- [ ] Regular user cannot upload (403)
- [ ] Unauthenticated user cannot upload (401)
- [ ] imageUrl updates in book response
- [ ] imageUrl becomes null after deletion
- [ ] Files stored with UUID names

---

## 🎯 Expected Results Summary

| Scenario | Method | Expected Status | File Action |
|----------|--------|-----------------|-------------|
| Upload valid image | POST | 200 OK | Saved |
| Upload invalid type | POST | 400 Bad Request | Not saved |
| Upload > 5MB | POST | 400 Bad Request | Not saved |
| Upload empty file | POST | 400 Bad Request | Not saved |
| Replace image | POST | 200 OK | Old deleted, new saved |
| Delete image | DELETE | 204 No Content | File deleted |
| View image | GET | 200 OK | Image file served |
| No auth | POST/DELETE | 401 Unauthorized | N/A |
| Wrong role | POST/DELETE | 403 Forbidden | N/A |

---

**Happy Testing! 🚀**

For questions or issues, check the server logs or test file validation messages.


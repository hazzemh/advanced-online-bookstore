# Image Upload Feature - Postman Testing Guide

## Overview
This guide demonstrates how to test the Image Upload endpoints using Postman. Admins can upload book cover images, delete them, and display them.

## Prerequisites
- Application running on `http://localhost:8080`
- Valid admin JWT token
- A book ID to upload image for
- Image file (JPEG, PNG, GIF, or WebP, max 5MB)

## Setup Instructions

1. **Get Admin Authentication Token**
   - Login as admin user
   - Use the JWT token in `Authorization` header

2. **Create a Book First**
   - Use admin endpoints to create a book
   - Note the book ID for image upload

## API Endpoints

### 1. Upload Image for a Book
**Endpoint:** `POST /api/admin/books/{bookId}/upload-image`
**Auth Required:** Yes (ADMIN role)

**Request Headers:**
```
Authorization: Bearer {ADMIN_JWT_TOKEN}
Content-Type: multipart/form-data
```

**Request Body (Form-Data):**
```
Key: image
Type: File
Value: [select image file from computer]
```

**Success Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "imagePath": "550e8400-e29b-41d4-a716-446655440001.jpg",
  "imageUrl": "/uploads/550e8400-e29b-41d4-a716-446655440001.jpg",
  // ... other book fields
}
```

**Error Responses:**
- **401 Unauthorized:** No JWT token or invalid token
- **403 Forbidden:** User is not admin
- **404 Not Found:** Book not found
- **400 Bad Request:** Invalid file (too big, wrong type, etc.)

### 2. Delete Image from a Book
**Endpoint:** `DELETE /api/admin/books/{bookId}/image`
**Auth Required:** Yes (ADMIN role)

**Request Headers:**
```
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**Success Response (204 No Content)**

**What happens:**
- Image file is deleted from disk
- Book's `imagePath` and `imageUrl` are set to null
- Book record is updated in database

### 3. Display/Serve Image
**Endpoint:** `GET /api/admin/books/image/{filename}`
**Auth Required:** No (public endpoint)

**Example:**
```
GET /api/admin/books/image/550e8400-e29b-41d4-a716-446655440001.jpg
```

**Response:**
- Binary image data
- Content-Type: image/jpeg
- Can be displayed directly in browser or `<img>` tag

**Frontend Usage:**
```html
<img src="http://localhost:8080/api/admin/books/image/550e8400-e29b-41d4-a716-446655440001.jpg"
     alt="Book Cover"
     width="200">
```

## Postman Setup

### Environment Variables
```
BASE_URL = http://localhost:8080
ADMIN_JWT_TOKEN = {your_admin_jwt_token}
BOOK_ID = {book_uuid_here}
IMAGE_FILENAME = {uploaded_image_filename}
```

### Collection Variables
Use these in your request URLs:
```
{{BASE_URL}}/api/admin/books/{{BOOK_ID}}/upload-image
{{BASE_URL}}/api/admin/books/{{BOOK_ID}}/image
{{BASE_URL}}/api/admin/books/image/{{IMAGE_FILENAME}}
```

### Authentication
Set Authorization tab to "Bearer Token" and use:
```
{{ADMIN_JWT_TOKEN}}
```

## Testing Scenarios

### Scenario 1: Complete Image Upload Flow
1. **Create a book** (if needed)
2. **Upload image** for the book
3. **Verify response** contains `imagePath` and `imageUrl`
4. **Display image** using the filename
5. **Delete image** from the book
6. **Verify book** no longer has image fields

### Scenario 2: File Validation Testing
Try uploading invalid files to test validation:

**Test Case 1: Empty File**
- Upload empty file
- Expect: 400 Bad Request - "File cannot be empty"

**Test Case 2: File Too Large**
- Upload file > 5MB
- Expect: 400 Bad Request - "File size exceeds maximum limit of 5MB"

**Test Case 3: Wrong File Type**
- Upload .exe or .txt file
- Expect: 400 Bad Request - "File type not allowed"

**Test Case 4: Malicious Filename**
- Upload file with name: `../../../etc/passwd.jpg`
- Expect: 400 Bad Request - "Invalid file name"

### Scenario 3: Authorization Testing

**Test Case 1: No Token**
- Try upload without Authorization header
- Expect: 401 Unauthorized

**Test Case 2: Regular User Token**
- Use regular user JWT (not admin)
- Expect: 403 Forbidden

**Test Case 3: Invalid Book ID**
- Use non-existent book UUID
- Expect: 404 Not Found

### Scenario 4: Image Replacement
1. Upload first image → Book gets `imagePath1`
2. Upload second image → Book gets `imagePath2`
3. Check that first image file is deleted from disk
4. Only second image should exist

### Scenario 5: Display Image
1. Upload image and get filename
2. Use GET endpoint to display image
3. Verify image loads in browser/Postman

## File Upload Details

### Supported Formats
- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- WebP (.webp)

### File Size Limit
- Maximum: 5MB per file
- Enforced by both client and server validation

### Storage Location
```
advanced-online-bookstore/
├── uploads/                    ← Created automatically
│   ├── 550e8400-e29b-41d4-a716-446655440001.jpg
│   ├── 550e8400-e29b-41d4-a716-446655440002.png
│   └── ... (other uploaded images)
```

### Filename Generation
- Uses UUID for uniqueness
- Preserves original extension
- Example: `550e8400-e29b-41d4-a716-446655440000.jpg`

## Security Features

### 1. Content-Type Validation
```java
if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
    throw new IllegalArgumentException("File type not allowed");
}
```
- Checks actual file type, not just filename extension
- Prevents: `malware.exe` renamed to `image.jpg`

### 2. Path Traversal Protection
```java
if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
    throw new IllegalArgumentException("Invalid file name");
}
```
- Prevents: `../../../etc/passwd` attacks
- Only allows safe filenames

### 3. Size Limits
```java
if (file.getSize() > MAX_FILE_SIZE) {
    throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
}
```
- Prevents disk space exhaustion
- Limits upload time

### 4. UUID Filenames
```java
String filename = UUID.randomUUID().toString() + "." + extension;
```
- Unpredictable filenames
- No guessing file locations
- Prevents direct access to sensitive files

### 5. Authorization
```java
@PreAuthorize("hasRole('ADMIN')")
```
- Only admins can upload/delete images
- Regular users cannot modify book images

## Error Handling

### Validation Errors (400 Bad Request)
- "File cannot be empty"
- "File size exceeds maximum limit of 5MB"
- "File type not allowed. Allowed types: JPEG, PNG, GIF, WebP"
- "Invalid file name"

### Authorization Errors
- **401 Unauthorized:** Missing or invalid JWT token
- **403 Forbidden:** User doesn't have ADMIN role

### Not Found Errors (404)
- **Book not found:** Invalid book ID
- **File not found:** Image filename doesn't exist

### Server Errors (500)
- "Failed to upload file" - Disk I/O error
- "Could not create upload directory" - Permission issues

## Frontend Integration

### HTML Display
```html
<!-- Display book cover -->
<img src="/api/admin/books/image/{{book.imagePath}}"
     alt="{{book.title}} cover"
     class="book-cover">

<!-- Upload form -->
<form action="/api/admin/books/{{bookId}}/upload-image"
      method="post"
      enctype="multipart/form-data">
    <input type="file" name="image" accept="image/*">
    <button type="submit">Upload Cover</button>
</form>
```

### JavaScript Upload
```javascript
const formData = new FormData();
formData.append('image', fileInput.files[0]);

fetch(`/api/admin/books/${bookId}/upload-image`, {
    method: 'POST',
    headers: {
        'Authorization': `Bearer ${adminToken}`
    },
    body: formData
})
.then(response => response.json())
.then(book => {
    // Update UI with new image
    imageElement.src = `/api/admin/books/image/${book.imagePath}`;
});
```

## Tips for Testing

1. **Use Postman Collections** to organize all image endpoints

2. **Test File Types:**
   - Try uploading different image formats
   - Try uploading non-image files (should fail)

3. **Test Size Limits:**
   - Find/create a file slightly over 5MB
   - Verify it's rejected

4. **Test Authorization:**
   - Try with regular user token
   - Try without any token

5. **Monitor Disk Space:**
   - Check `uploads/` folder after uploads
   - Verify files are deleted when replaced

6. **Browser Testing:**
   - Copy image URL to browser
   - Verify images display correctly

## Complete Testing Checklist

- [ ] Upload valid JPEG image
- [ ] Upload valid PNG image
- [ ] Upload valid GIF image
- [ ] Upload valid WebP image
- [ ] Try upload empty file (should fail)
- [ ] Try upload >5MB file (should fail)
- [ ] Try upload .exe file (should fail)
- [ ] Try upload with malicious filename (should fail)
- [ ] Try upload without admin token (should fail)
- [ ] Try upload with regular user token (should fail)
- [ ] Try upload for non-existent book (should fail)
- [ ] Upload image, then upload replacement (old file deleted)
- [ ] Delete image from book
- [ ] Display uploaded image in browser
- [ ] Verify image appears in book details API response

This completes the Image Upload feature implementation and testing guide!

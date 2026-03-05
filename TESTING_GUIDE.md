# Authentication Testing Guide 🧪

This guide explains how to test the authentication features you just implemented in your Spring Boot application.

## Table of Contents
1. [Unit Testing](#unit-testing)
2. [Manual Testing with Postman](#manual-testing-with-postman)
3. [Manual Testing with cURL](#manual-testing-with-curl)
4. [Testing Protected Endpoints](#testing-protected-endpoints)

---

## Unit Testing

### What We Created
A comprehensive test class `AuthControllerTest.java` that tests:
- ✅ User registration with valid data
- ✅ Duplicate email prevention
- ✅ User login with correct credentials
- ✅ Login failure with invalid email
- ✅ Login failure with invalid password
- ✅ JWT token format validation
- ✅ JWT token validity and username extraction

### How to Run Unit Tests

**Option 1: Using IDE (IntelliJ)**
1. Navigate to: `src/test/java/com/example/bookstore/user/controller/AuthControllerTest.java`
2. Right-click on the class name
3. Click "Run 'AuthControllerTest'"
4. View results in the test panel (should show all tests passing ✅)

**Option 2: Using Maven Command**
```powershell
# Navigate to project directory
cd "G:\Hazzem\Java Projects\advanced-online-bookstore"

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthControllerTest

# Run specific test method
mvn test -Dtest=AuthControllerTest#testRegisterSuccess
```

**Option 3: Using IDE Terminal**
```powershell
# In JetBrains terminal
./mvnw test
```

### What Each Test Does

| Test Name | Purpose | Expected Result |
|-----------|---------|-----------------|
| `testRegisterSuccess` | Tests valid registration | Status 200, returns JWT token |
| `testRegisterWithDuplicateEmail` | Tests duplicate email rejection | Second registration fails with 500 |
| `testLoginSuccess` | Tests valid login | Status 200, returns JWT token |
| `testLoginWithInvalidEmail` | Tests login with non-existent user | Status 4xx error |
| `testLoginWithInvalidPassword` | Tests login with wrong password | Status 4xx error |
| `testTokenFormat` | Validates JWT structure (3 parts) | Token contains valid JWT format |
| `testJwtTokenValidity` | Tests token validation and username extraction | Token is valid, username matches |

---

## Manual Testing with Postman

### Prerequisites
1. Download and install [Postman](https://www.postman.com/downloads/)
2. Start your Spring Boot application:
   - In IntelliJ: Click the green "Run" button or press `Shift+F10`
   - Or in terminal: `mvn spring-boot:run`
3. Server should run on `http://localhost:8080`

### Test Case 1: Register a New User

**Request:**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "MySecurePassword123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Expected Response (Status: 200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTc0MDY3MjAwMCwiZXhwIjoxNzQwNzU4NDAwfQ.ZjK8..."
}
```

### Test Case 2: Try to Register with Duplicate Email

**Request:**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "AnotherPassword123!",
  "firstName": "Jane",
  "lastName": "Doe"
}
```

**Expected Response (Status: 500 Internal Server Error):**
```json
{
  "error": "Email already exists"
}
```

### Test Case 3: Login with Registered User

**Request:**
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "MySecurePassword123!"
}
```

**Expected Response (Status: 200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTc0MDY3MjAwMCwiZXhwIjoxNzQwNzU4NDAwfQ.ZjK8..."
}
```

### Test Case 4: Login with Wrong Password

**Request:**
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "WrongPassword123!"
}
```

**Expected Response (Status: 401 Unauthorized):**
```json
{
  "error": "Bad credentials"
}
```

### Test Case 5: Save Token and Use It

1. Copy the token from any successful login/register response
2. Create a new request to a protected endpoint (you can test this once you have protected endpoints)
3. In the Headers tab, add:
   - Key: `Authorization`
   - Value: `Bearer <your_token_here>`
   
**Example:**
```
GET http://localhost:8080/api/users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTc0MDY3MjAwMCwiZXhwIjoxNzQwNzU4NDAwfQ.ZjK8...
```

---

## Manual Testing with cURL

**Option 1: Register User (Command Line)**
```powershell
$body = @{
    email = "alice.smith@example.com"
    password = "SecurePass456!"
    firstName = "Alice"
    lastName = "Smith"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $body
```

**Option 2: Login User (Command Line)**
```powershell
$body = @{
    email = "alice.smith@example.com"
    password = "SecurePass456!"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $body
```

---

## Testing Protected Endpoints

### What This Tests
Once you add `@GetMapping` or other protected endpoints, they will:
1. Check for `Authorization` header with Bearer token
2. Validate the token using `JwtAuthenticationFilter`
3. Extract username and load user details
4. Allow/deny access based on authentication

### Example Flow
```
1. User registers/logs in → receives JWT token
2. User makes request to protected endpoint → includes token in Authorization header
3. JwtAuthenticationFilter intercepts request → validates token
4. If valid → request continues to controller
5. If invalid → request is rejected with 401 Unauthorized
```

### Test Protected Endpoint (Once Created)
```
GET http://localhost:8080/api/books
Authorization: Bearer <token_from_register_or_login>

Response: 200 OK (returns books)
```

Without Authorization header:
```
GET http://localhost:8080/api/books

Response: 401 Unauthorized
```

---

## Troubleshooting

### Common Issues

**1. "Connection refused" error**
- Solution: Make sure the Spring Boot application is running on localhost:8080

**2. "User not found" on login**
- Solution: Ensure you registered the user first before logging in

**3. "Bad credentials"**
- Solution: Check that email and password match exactly (case-sensitive password)

**4. Token validation fails**
- Solution: Ensure token hasn't expired (24 hours by default, set in `application.yml`)
- Token might be malformed if copied with extra spaces

**5. Tests fail with database errors**
- Solution: Tests use H2 in-memory database by default. Check if PostgreSQL is running if configured for main app

---

## Key Points to Remember

✅ **Always send tokens** in Authorization header as: `Bearer <token>`
✅ **Tokens are time-limited** (24 hours default)
✅ **Passwords are encrypted** using BCrypt before storage
✅ **Email must be unique** per user
✅ **Stateless auth** - server doesn't store session info
✅ **Each request is authenticated independently** via JWT

---

## Next Steps

Once confident with these tests, consider:
1. Creating integration tests for repositories
2. Adding unit tests for JwtService
3. Testing with invalid JWT formats
4. Load testing with multiple concurrent requests
5. Testing with expired tokens (modify expiration in config)


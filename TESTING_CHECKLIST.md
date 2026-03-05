# 🧪 Authentication Testing Checklist

## Pre-Testing Checklist
- [ ] PostgreSQL database is running (if using production config)
- [ ] JDK 21+ is installed
- [ ] Maven is installed
- [ ] IDE (IntelliJ) is open with the project
- [ ] No other service is using port 8080

---

## Method 1: Unit Testing ✓

### Running Tests
- [ ] Open AuthControllerTest.java
- [ ] Right-click on class → "Run 'AuthControllerTest'"
- [ ] OR run: `mvn test -Dtest=AuthControllerTest`
- [ ] OR run: `mvn test` (all tests)

### Expected Results
- [ ] ✅ testRegisterSuccess - PASSED
- [ ] ✅ testRegisterWithDuplicateEmail - PASSED
- [ ] ✅ testLoginSuccess - PASSED
- [ ] ✅ testLoginWithInvalidEmail - PASSED
- [ ] ✅ testLoginWithInvalidPassword - PASSED
- [ ] ✅ testTokenFormat - PASSED
- [ ] ✅ testJwtTokenValidity - PASSED

**Status**: All tests should pass (7/7)

---

## Method 2: Postman Testing 📮

### Setup
- [ ] Install Postman from https://www.postman.com/downloads/
- [ ] Start the application (click RUN in IDE or `mvn spring-boot:run`)
- [ ] Import Postman_Collection.json (File → Import → Select file)

### Test Cases

#### Test 1: Register New User ✓
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "testuser@example.com",
  "password": "Password123!",
  "firstName": "Test",
  "lastName": "User"
}
```
- [ ] Status Code: 200 OK
- [ ] Response contains "token" field
- [ ] Token is not empty
- [ ] Token format: `eyJ...` (starts with eyJ)

#### Test 2: Login with Correct Credentials ✓
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "testuser@example.com",
  "password": "Password123!"
}
```
- [ ] Status Code: 200 OK
- [ ] Response contains "token" field
- [ ] Token is valid JWT (3 parts separated by dots)

#### Test 3: Duplicate Email Registration ✗ (Should Fail)
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "testuser@example.com",
  "password": "DifferentPassword!",
  "firstName": "Another",
  "lastName": "User"
}
```
- [ ] Status Code: 500 Internal Server Error
- [ ] Error message: "Email already exists"

#### Test 4: Wrong Password Login ✗ (Should Fail)
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "testuser@example.com",
  "password": "WrongPassword!"
}
```
- [ ] Status Code: 401 Unauthorized
- [ ] Error indicates "Bad credentials"

#### Test 5: Non-existent User Login ✗ (Should Fail)
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "doesnotexist@example.com",
  "password": "AnyPassword!"
}
```
- [ ] Status Code: 401 Unauthorized or 404 Not Found
- [ ] Error message related to user not found

---

## Method 3: PowerShell Testing 💻

### Test 1: Register
```powershell
$body = @{
    email = "pwshuser@example.com"
    password = "Password123!"
    firstName = "PowerShell"
    lastName = "User"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $body

$response.StatusCode  # Should be 200
$response.Content | ConvertFrom-Json  # Should show token
```
- [ ] Response status: 200
- [ ] Token received and contains "."

### Test 2: Login
```powershell
$body = @{
    email = "pwshuser@example.com"
    password = "Password123!"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -Headers @{"Content-Type"="application/json"} `
    -Body $body

$response.StatusCode  # Should be 200
$token = ($response.Content | ConvertFrom-Json).token
Write-Host "Token: $token"
```
- [ ] Response status: 200
- [ ] Token extracted successfully

---

## Method 4: Browser Network Inspector 🌐

### Manual Web Testing
1. [ ] Open developer tools (F12) in any browser
2. [ ] Go to Console tab
3. [ ] Make POST request using Fetch API:

```javascript
// Register
fetch('http://localhost:8080/api/auth/register', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    email: 'jsuser@example.com',
    password: 'Password123!',
    firstName: 'JS',
    lastName: 'User'
  })
})
.then(r => r.json())
.then(data => console.log(data));

// Login
fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    email: 'jsuser@example.com',
    password: 'Password123!'
  })
})
.then(r => r.json())
.then(data => console.log(data));
```
- [ ] Check Console for token response
- [ ] Verify token format

---

## Verification Checklist

### JWT Token Structure
- [ ] Token has 3 parts (header.payload.signature)
- [ ] Can be decoded at https://jwt.io/
- [ ] Contains username in payload
- [ ] Has expiration time
- [ ] Has issued-at timestamp

### Security Verification
- [ ] Passwords are NOT visible in database (stored as bcrypt hash)
- [ ] Duplicate emails are rejected
- [ ] Wrong passwords are rejected
- [ ] Tokens are not reusable after expiration
- [ ] Each token is unique (even for same user at different times)

### Error Handling
- [ ] Invalid email format shows appropriate error
- [ ] Missing fields show validation error
- [ ] Expired token (wait 24 hours) shows 401 error
- [ ] Malformed token shows 401 error

---

## Debugging Tips

### If Tests Fail

**Test: "Connection refused"**
- Verify app is running on localhost:8080
- Check no firewall is blocking port 8080

**Test: "User not found"**
- Make sure you registered before logging in
- Check email is exactly the same (case-sensitive)

**Test: "Email already exists"**
- This is EXPECTED for duplicate test
- Use different email for new registrations

**Test: Token is invalid**
- Token might be expired (24-hour TTL)
- Token might have extra spaces when copied
- Verify token hasn't been modified

**Application won't start**
- Check PostgreSQL is running (if not using H2)
- Check no other app is using port 8080
- Check Maven dependencies: `mvn dependency:resolve`

---

## Performance Testing (Optional)

### Load Test (Multiple Registrations)
```powershell
for ($i = 1; $i -le 10; $i++) {
    $body = @{
        email = "user$i@example.com"
        password = "Password123!"
        firstName = "User$i"
        lastName = "Test"
    } | ConvertTo-Json
    
    Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body $body -OutFile $null
    
    Write-Host "Created user$i"
}
```
- [ ] All requests return status 200
- [ ] No timeout errors
- [ ] Response time < 500ms per request

---

## Final Sign-Off

- [ ] All unit tests passing
- [ ] All Postman tests returning expected status codes
- [ ] Token generation working
- [ ] Token validation working
- [ ] User registration with password hashing working
- [ ] Duplicate email prevention working
- [ ] Login with credentials working
- [ ] Ready to integrate with protected endpoints

---

## Next Steps After Testing
1. Create protected endpoints (e.g., GET /api/users/profile)
2. Add authorization levels (USER, ADMIN roles)
3. Create integration tests for repositories
4. Add password reset functionality
5. Add email verification
6. Add rate limiting on auth endpoints
7. Deploy to production

---

**Document created**: 2026-03-05
**Last updated**: 2026-03-05
**Status**: Ready for Testing ✅


# 🎯 Visual Testing Flow Guide

## Overall Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    HTTP CLIENT (Browser/Postman/cURL)       │
└────────────────┬────────────────────────────────────────────┘
                 │ HTTP Request
                 ▼
┌─────────────────────────────────────────────────────────────┐
│              SPRING BOOT APPLICATION (Port 8080)             │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │          AuthController                              │  │
│  │  ├─ @PostMapping("/register")                        │  │
│  │  └─ @PostMapping("/login")                           │  │
│  └──────────┬───────────────────────────────────────────┘  │
│             │                                               │
│  ┌──────────▼───────────────────────────────────────────┐  │
│  │          UserService                                 │  │
│  │  ├─ register(RegisterRequest)                        │  │
│  │  └─ validates email uniqueness                       │  │
│  │  └─ encodes password with BCrypt                     │  │
│  └──────────┬───────────────────────────────────────────┘  │
│             │                                               │
│  ┌──────────▼───────────────────────────────────────────┐  │
│  │          UserRepository                              │  │
│  │  ├─ findByEmail(String email)                        │  │
│  │  └─ existsByEmail(String email)                      │  │
│  │  └─ save(User user)                                  │  │
│  └──────────┬───────────────────────────────────────────┘  │
│             │                                               │
│  ┌──────────▼───────────────────────────────────────────┐  │
│  │          JwtService                                  │  │
│  │  ├─ generateToken(String username)                   │  │
│  │  ├─ validateToken(String token)                      │  │
│  │  └─ extractUsername(String token)                    │  │
│  └──────────┬───────────────────────────────────────────┘  │
│             │                                               │
│  ┌──────────▼───────────────────────────────────────────┐  │
│  │          PostgreSQL Database                         │  │
│  │  └─ users table (email, password_hash, role, etc.)   │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                 ▲
                 │ HTTP Response (JSON with token)
                 │
┌─────────────────────────────────────────────────────────────┐
│                   HTTP CLIENT                               │
│          ← Status: 200, Token: "eyJ..."                    │
└─────────────────────────────────────────────────────────────┘
```

---

## Registration Flow

```
USER                          POSTMAN/CURL              SPRING BOOT
  │                               │                          │
  ├─ Fill form                    │                          │
  │                               │                          │
  └─ Click "Register" ────POST────► /api/auth/register ─┐   │
                                  │                      │   │
                                  │  ┌──────────────────►│   │
                                  │  │  1. Receive       │   │
                                  │  │     RegisterRequest
                                  │  │                   │   │
                                  │  │  2. Validate      │   │
                                  │  │     (email unique) │   │
                                  │  │                   │   │
                                  │  │  3. Hash password │   │
                                  │  │     (BCrypt)      │   │
                                  │  │                   │   │
                                  │  │  4. Save user     │   │
                                  │  │     to database   │   │
                                  │  │                   │   │
                                  │  │  5. Generate JWT  │   │
                                  │  │     token         │   │
                                  │  │                   │   │
                                  │  └──────────────────┐│   │
                                  │                     ││   │
                       ◄─ 200 OK ◄─ {"token":"......"} ◄┘   │
                    {"token":"eyJ..."}
  │
  └─ Copy token for future use
```

---

## Login Flow

```
USER                          POSTMAN/CURL              SPRING BOOT
  │                               │                          │
  ├─ Enter email                  │                          │
  ├─ Enter password               │                          │
  │                               │                          │
  └─ Click "Login" ────POST──────► /api/auth/login ──┐      │
                                  │                  │      │
                                  │  ┌───────────────►│      │
                                  │  │  1. Receive    │      │
                                  │  │     LoginRequest
                                  │  │                │      │
                                  │  │  2. Find user  │      │
                                  │  │     by email   │      │
                                  │  │                │      │
                                  │  │  3. Compare    │      │
                                  │  │     passwords  │      │
                                  │  │     (BCrypt)   │      │
                                  │  │                │      │
                                  │  │  4. Generate   │      │
                                  │  │     JWT token  │      │
                                  │  │                │      │
                                  │  └───┬────────────┘      │
                                  │      │                   │
                       ◄─ 200 OK ◄───────┘ {"token":"......"}│
                    {"token":"eyJ..."}
  │
  └─ Use token in Authorization header for protected requests
```

---

## JWT Token Structure

```
┌──────────────────────────────────────────────────────────────┐
│  JWT Token Example:                                          │
│  eyJhbGciOiJIUzI1NiJ9.                                       │
│  eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzQwNjcyMDAwfQ. │
│  ZjK8LjEiOiIxNzQwNzU4NDAwIn0                                 │
└──────────────────────────────────────────────────────────────┘

         ▼         ▼         ▼
    HEADER   +  PAYLOAD  +  SIGNATURE
    (Base64)   (Base64)   (HMAC-SHA256)

┌──────────────────┬────────────────────────────┬───────────┐
│ HEADER           │ PAYLOAD                    │ SIGNATURE │
├──────────────────┼────────────────────────────┼───────────┤
│ {                │ {                          │ Generated │
│   "alg":         │   "sub":                   │ using:    │
│   "HS256"        │   "john@example.com",      │ • Secret  │
│ }                │   "iat": 1740672000,       │ • HMAC    │
│                  │   "exp": 1740758400        │ • SHA256  │
│                  │ }                          │           │
└──────────────────┴────────────────────────────┴───────────┘

Decoded at: https://jwt.io/
```

---

## Protected Endpoint Flow

```
USER                      POSTMAN/CURL            SPRING BOOT
  │ (has token)              │                        │
  │                          │                        │
  └─ Make request ──────────► GET /api/books ────┐   │
     with token in header    Authorization:      │   │
                             Bearer <token> ─────┼──►│
                                │                │   │
                                │  ┌──────────┐  │   │
                                │  │  Check   │  │   │
                                │  │  Authorization
                                │  │  header  │  │   │
                                │  └──┬───────┘  │   │
                                │     │          │   │
                                │     ▼          │   │
                                │  Extract token │   │
                                │  "eyJ..."      │   │
                                │     │          │   │
                                │     ▼          │   │
                                │  JwtFilter     │   │
                                │  validates     │   │
                                │  token         │   │
                                │     │          │   │
                                │     ├─ Valid?  │   │
                                │     │  YES ──┐ │   │
                                │     │        │ │   │
                                │     ▼        │ │   │
                                │  Extract     │ │   │
                                │  username    │ │   │
                                │     │        │ │   │
                                │     ▼        │ │   │
                                │  Load user   │ │   │
                                │  details     │ │   │
                                │     │        │ │   │
                                │     ▼        │ │   │
                                │  Set in      │ │   │
                                │  Security    │ │   │
                                │  Context     │ │   │
                                │     │        │ │   │
                                │     └────┐   │ │   │
                                │          │   │ │   │
                                │  ┌───────▼─┐ │ │   │
                                │  │ Allow   │ │ │   │
                                │  │ request │ │ │   │
                                │  │ to pass │ │ │   │
                                │  └─────────┘ │ │   │
                                │              │ │   │
                       ◄───────────────────────┘ │   │
                    200 OK {"data": ...}
```

---

## Test Execution Sequence

### Unit Test Flow
```
┌─────────────────────────────────────────────┐
│ mvn test -Dtest=AuthControllerTest          │
└────────────┬────────────────────────────────┘
             │
             ▼
    ┌────────────────────┐
    │ Start Maven        │
    │ Load dependencies  │
    └────────┬───────────┘
             │
             ▼
    ┌────────────────────────────────┐
    │ Spring Boot starts test context │
    │ (in-memory database)           │
    └────────┬───────────────────────┘
             │
             ▼
    ┌──────────────────────────────┐
    │ Execute Test 1:              │
    │ testRegisterSuccess          │
    │                              │
    │ ✅ PASSED                    │
    └──────────────────────────────┘
             │
             ▼
    ┌──────────────────────────────┐
    │ Execute Test 2:              │
    │ testRegisterWithDuplicateEmail
    │                              │
    │ ✅ PASSED                    │
    └──────────────────────────────┘
             │
             ▼
    ┌──────────────────────────────┐
    │ ... (Tests 3-7) ...          │
    │                              │
    │ ✅ ALL PASSED                │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │ BUILD SUCCESS                │
    │ Tests run: 7                 │
    │ Failures: 0                  │
    │ Errors: 0                    │
    └──────────────────────────────┘
```

---

## Testing Methods Comparison

```
┌──────────────────┬──────────┬──────────┬─────────────┬──────────┐
│ Method           │ Speed    │ Ease     │ Automation  │ Learning │
├──────────────────┼──────────┼──────────┼─────────────┼──────────┤
│ Unit Test        │ ⚡⚡⚡   │ ⭐⭐⭐   │ ✅✅✅      │ ⭐⭐    │
│ (Maven)          │ Very     │ Very     │ Full        │ Good     │
│                  │ Fast     │ Easy     │             │          │
├──────────────────┼──────────┼──────────┼─────────────┼──────────┤
│ Postman          │ ⚡⚡    │ ⭐⭐⭐   │ Partial     │ ⭐⭐⭐  │
│ (Manual/Visual)  │ Fast     │ Very     │ Can save    │ Excellent│
│                  │          │ Easy     │ requests    │          │
├──────────────────┼──────────┼──────────┼─────────────┼──────────┤
│ PowerShell       │ ⚡⚡⚡   │ ⭐⭐    │ ✅✅       │ ⭐⭐    │
│ (Command Line)   │ Very     │ Easy     │ Can script  │ Good     │
│                  │ Fast     │ (if copy-paste) │        │    │
├──────────────────┼──────────┼──────────┼─────────────┼──────────┤
│ Browser Console  │ ⚡⚡    │ ⭐⭐    │ Limited     │ ⭐⭐    │
│ (JavaScript)     │ Fast     │ Medium   │ Ad-hoc      │ Good     │
└──────────────────┴──────────┴──────────┴─────────────┴──────────┘

RECOMMENDED: Start with Unit Tests (simple, automated)
           Then try Postman (visual, intuitive)
           Then PowerShell (scriptable, advanced)
```

---

## Data Flow During Registration

```
┌─────────────────────────┐
│ User Input              │
│ email: john@example.com │
│ password: Pass123!      │
│ firstName: John         │
│ lastName: Doe           │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│ RegisterRequest DTO                 │
│ (Transport object)                  │
│ ├─ email: "john@example.com"        │
│ ├─ password: "Pass123!"             │
│ ├─ firstName: "John"                │
│ └─ lastName: "Doe"                  │
└──────────┬──────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│ UserService.register()              │
│ ├─ Check if email exists            │
│ └─ Hash password: BCrypt            │
│    "Pass123!" ──────► "$2a$10$..."  │
└──────────┬──────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│ User Entity                         │
│ ├─ id: UUID (auto-generated)        │
│ ├─ email: "john@example.com"        │
│ ├─ password: "$2a$10$..."(hashed)   │
│ ├─ firstName: "John"                │
│ ├─ lastName: "Doe"                  │
│ ├─ role: "USER"                     │
│ ├─ enabled: true                    │
│ ├─ createdAt: 2026-03-05 19:38...   │
│ └─ updatedAt: 2026-03-05 19:38...   │
└──────────┬──────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│ Save to Database                    │
│ PostgreSQL: INSERT INTO users...    │
└──────────┬──────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│ JwtService.generateToken()          │
│                                     │
│ Input: "john@example.com"           │
│        ↓                            │
│ Create JWT claims:                  │
│ ├─ sub: "john@example.com"          │
│ ├─ iat: 1740672000 (now)            │
│ └─ exp: 1740758400 (24h later)      │
│        ↓                            │
│ Sign with secret key                │
│        ↓                            │
│ Output: "eyJhbGciOiJIUzI1NiJ9..."  │
└──────────┬──────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│ AuthResponse DTO                    │
│ {                                   │
│   "token": "eyJhbGciOiJIUzI1NiJ9..."│
│ }                                   │
└──────────┬──────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│ HTTP Response                       │
│ Status: 200 OK                      │
│ Body: {"token":"eyJ..."}            │
└─────────────────────────────────────┘
```

---

## Security Layers

```
┌─────────────────────────────────────────────────┐
│           HTTP Request                          │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
        ┌──────────────────────┐
        │ JwtAuthenticationFilter
        │ - Extract token      │
        │ - Validate signature │
        │ - Check expiration   │
        └──────┬───────────────┘
               │
        ✅ Valid?
        │   │   │
        │   │   └─── ❌ Invalid → 401 Unauthorized
        │   │
        │   ✅ Valid
        │       │
        ▼       ▼
        ┌──────────────────────┐
        │ Load User Details    │
        │ - Get username       │
        │ - Get authorities    │
        └──────┬───────────────┘
               │
               ▼
        ┌──────────────────────┐
        │ Set SecurityContext  │
        │ - User is "logged in"│
        │ - Available to app   │
        └──────┬───────────────┘
               │
               ▼
        ┌──────────────────────┐
        │ Controller Method    │
        │ - Processes request  │
        │ - Returns response   │
        └──────┬───────────────┘
               │
               ▼
┌──────────────────────────────────────────────────┐
│           HTTP Response                         │
│           Status: 200                           │
│           Body: {...}                           │
└──────────────────────────────────────────────────┘
```

---

## File Organization

```
advanced-online-bookstore/
│
├── src/
│   ├── main/
│   │   └── java/com/example/bookstore/
│   │       ├── config/
│   │       │   └── SecurityBeansConfig.java
│   │       │
│   │       ├── security/
│   │       │   ├── config/
│   │       │   │   └── SecurityConfig.java ✅ (Configures security)
│   │       │   └── jwt/
│   │       │       ├── JwtService.java ✅ (Generates/validates tokens)
│   │       │       └── JwtAuthenticationFilter.java ✅ (Intercepts requests)
│   │       │
│   │       └── user/
│   │           ├── controller/
│   │           │   └── AuthController.java ✅ (Handles /api/auth/*)
│   │           │
│   │           ├── service/
│   │           │   ├── UserService.java ✅ (Business logic)
│   │           │   └── UserDetailsServiceImpl.java ✅ (Spring Security integration)
│   │           │
│   │           ├── repository/
│   │           │   └── UserRepository.java ✅ (Database access)
│   │           │
│   │           ├── entity/
│   │           │   ├── User.java ✅ (Database model)
│   │           │   └── Role.java
│   │           │
│   │           └── dto/
│   │               ├── AuthResponse.java ✅ (Response model)
│   │               ├── LoginRequest.java ✅ (Request model)
│   │               └── RegisterRequest.java ✅ (Request model)
│   │
│   └── test/
│       └── java/com/example/bookstore/
│           └── user/controller/
│               └── AuthControllerTest.java ✅ (7 unit tests)
│
├── TESTING_GUIDE.md ✅ (Full testing guide)
├── TESTING_CHECKLIST.md ✅ (Interactive checklist)
├── QUICK_START_TESTING.md ✅ (Fast overview)
├── COMMANDS_REFERENCE.md ✅ (All commands)
└── Postman_Collection.json ✅ (Ready to import)
```

---

**Visual guide complete! Use the diagrams above to understand the flow of your authentication system. 🎨**


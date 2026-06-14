# 🔐 Spring Security JWT

Stateless authentication API built with **Java 21**, **Spring Boot 3** and **Spring Security**.  
Implements JWT-based login, token validation, refresh tokens and role-based access control (RBAC).

---

## 🏗️ Architecture

```
[Client]
   │
   ├── POST /api/auth/register → creates user + returns JWT
   ├── POST /api/auth/login    → authenticates + returns JWT
   ├── POST /api/auth/refresh  → exchanges refresh token for new access token
   │
   └── GET /api/me             → protected (any authenticated user)
   └── GET /api/user/profile   → protected (ROLE_USER or ROLE_ADMIN)
   └── GET /api/admin/dashboard → protected (ROLE_ADMIN only)

Every protected request passes through JwtAuthenticationFilter:
  Authorization: Bearer <token> → validates → sets SecurityContext
```

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Language |
| Spring Boot 3.5 | Framework |
| Spring Security 6 | Authentication & Authorization |
| JJWT 0.12 | JWT generation and validation |
| PostgreSQL 16 | User persistence |
| BCrypt | Password hashing |
| Docker Compose | Local database |

---

## ▶️ Running Locally

### Prerequisites
- Docker Desktop
- Java 21
- Maven

### Steps

**1. Start database**
```bash
docker-compose up -d
```

**2. Create `.env` file**
```
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

**3. Run the application**
```bash
./mvnw spring-boot:run
```

**4. Run tests**
```bash
./mvnw test
```

---

## 📮 Endpoints

### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@email.com",
  "password": "secret123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "email": "john@email.com",
  "role": "ROLE_USER"
}
```

---

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@email.com",
  "password": "secret123"
}
```

---

### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGci..."
}
```

---

### Protected Routes

```http
GET /api/me
Authorization: Bearer <accessToken>
```

```http
GET /api/user/profile
Authorization: Bearer <accessToken>
# Requires ROLE_USER or ROLE_ADMIN
```

```http
GET /api/admin/dashboard
Authorization: Bearer <accessToken>
# Requires ROLE_ADMIN → returns 403 for ROLE_USER
```

---

## 🔄 Authentication Flow

```
1. Client sends credentials → POST /api/auth/login
2. Server validates password with BCrypt
3. Server generates access token (24h) + refresh token (7 days)
4. Client stores tokens and sends: Authorization: Bearer <token>
5. JwtAuthenticationFilter intercepts every request
6. Filter extracts username from token → loads user from DB
7. Validates token signature and expiration
8. Sets authentication in SecurityContextHolder
9. Spring Security checks roles for the requested endpoint
```

---

## 💡 Key Concepts Demonstrated

| Concept | Where |
|---|---|
| Stateless authentication | `SessionCreationPolicy.STATELESS` in `SecurityConfig` |
| JWT generation & validation | `JwtService` — HMAC-SHA384 signing |
| Custom security filter | `JwtAuthenticationFilter extends OncePerRequestFilter` |
| Role-based access (RBAC) | `@PreAuthorize` + `hasRole()` in `SecurityConfig` |
| Password hashing | `BCryptPasswordEncoder` |
| Refresh token | Separate long-lived token to renew access token |
| UserDetails integration | `User implements UserDetails` |

---

## 🧪 Tests

| Test | Type | What it covers |
|---|---|---|
| `JwtServiceTest` | Unit | Token generation, validation, username extraction, expiration, invalid token |
| `AuthServiceTest` | Unit | Register, duplicate email, login, refresh token, invalid refresh |

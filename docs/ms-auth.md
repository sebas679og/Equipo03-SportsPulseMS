# 🟣 ms-auth — Authentication Service

[← Back to README](../README.md)

## Overview

**Port:** `8081`  
**Database:** PostgreSQL (`auth_db`)

The `ms-auth` service manages user registration and authentication. It is responsible for issuing JWT tokens that all other domain microservices must validate on every protected request.

---

## Data Model

### Entity: `User`

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Unique identifier |
| `username` | String | Username (unique, no spaces) |
| `email` | String | Email address (unique) |
| `password` | String | Bcrypt hash of the password |
| `role` | Enum | User role |
| `createdAt` | Instant | Registration timestamp |
| `updatedAt` | Instant | Last update timestamp |

### Enums

```
UserRole: USER, ADMIN
```

---

## Endpoints

### 1. `POST /api/auth/register`

**Access:** Public

**Description:** Registers a new user in the system.

**Request Body:**
```json
{
  "username": "javier_ruiz",
  "email": "javier@email.com",
  "password": "SportPass123!"
}
```

**Validation Rules:**
- Email must be unique and in a valid format.
- Password must be at least 8 characters, contain at least one uppercase letter and one number.
- Username must be unique and contain no spaces.

**Response `201 Created`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "javier_ruiz",
  "email": "javier@email.com",
  "role": "USER",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

**Response `409 Conflict`** — when the email or username is already taken:
```json
{
  "code": 409,
  "name": "CONFLICT",
  "description": "A user with that email already exists",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

---

### 2. `POST /api/auth/login`

**Access:** Public

**Description:** Authenticates a user and returns a JWT token.

**Request Body:**
```json
{
  "email": "javier@email.com",
  "password": "SportPass123!"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response `401 Unauthorized`:**
```json
{
  "code": 401,
  "name": "UNAUTHORIZED",
  "description": "Incorrect email or password",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

---

### 3. `GET /api/auth/validate`

**Access:** Internal (consumed by other microservices)

**Description:** Validates a JWT token and returns the associated user information. This endpoint is used exclusively for inter-service communication and must not be exposed to public clients.

**Required Header:**
```
Authorization: Bearer <token>
X-Internal-API-Key: <service-api-key>
```

**Response `200 OK`:**
```json
{
  "valid": true,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "javier_ruiz",
  "role": "USER"
}
```

**Response `401 Unauthorized`:**
```json
{
  "code": 401,
  "name": "UNAUTHORIZED",
  "description": "Invalid or missing internal API key",
  "timestamp": "2025-01-15T10:30:000Z"
}
```

**Response `403 Forbiden`:**

```json
{
  "code": 403,
  "name": "FORBIDDEN",
  "description": "Invalid internal API key",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

| Case | Status |
|------|--------|
| Missing `Authorization` | 401 |
| Invalid JWT | 401 |
| Missing `X-Internal-API-Key` | 401 |
| Invalid API Key | 403 |

---

## JWT Validation Strategy

Other microservices can validate JWT tokens in the following way:

- **Remotely:** Call `GET /api/auth/validate` on `ms-auth` with the service's apikey.

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `SPORTS_PULSE_DATASOURCE_URL_AUTH` | PostgreSQL connection URL for auth service | `jdbc:postgresql://postgres-auth:5432/auth_db` |
| `SPORTS_PULSE_DATASOURCE_USERNAME` | Database username | `admin` |
| `SPORTS_PULSE_DATASOURCE_PASSWORD` | Database password | `admin123` |
| `SPORTS_PULSE_JWT_SECRET` | Base64-encoded secret key used to sign JWT tokens (generated with `openssl rand -base64 32`) | `Y3VzdG9tLXNlY3JldC1rZXktZXhhbXBsZQ==` |
| `SPORTS_PULSE_JWT_EXPIRATION` | Token expiry in seconds | `3600` |
| `SPORTS_PULSE_TOKEN_TYPE` | Token type used in Authorization header | `Bearer` |
| `SPORTS_PULSE_INTERNAL_API_KEY` | Hexadecimal API key for internal microservice communication (generated with `openssl rand -hex 32`) | `a3f5c9d8e1b2476f9a0c1234d5e6f789abcd1234ef567890abcd1234ef567890` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `SPORTS_PULSE_JPA_DDL` | Hibernate DDL mode | `validate` |
| `SPORTS_PULSE_JPA_SHOW_SQL` | Enable SQL logging | `false` |
| `SPORTS_PULSE_LEVEL_LOGIN` | Logging level for application | `INFO` |
| `SWAGGER_UI_DOCUMENTATION_ENABLED` | Enable Swagger/OpenAPI documentation | `false` |

---

## Notes

- Passwords are never stored in plain text; bcrypt hashing is mandatory.
- The `GET /api/auth/validate` endpoint should be treated as internal and ideally not exposed through the gateway to end users.
- Token expiration is set to **3600 seconds (1 hour)** by default.
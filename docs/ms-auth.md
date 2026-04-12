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

**Description:** Validates a JWT token and returns the associated user information. This is the endpoint called by other services to verify that the token received in a request is valid.

**Required Header:**
```
Authorization: Bearer <token>
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

---

## JWT Validation Strategy

Other microservices can validate JWT tokens in one of two ways:

- **Locally:** Verify the token signature using the shared secret key available via the `JWT_SECRET` environment variable.
- **Remotely:** Call `GET /api/auth/validate` on `ms-auth` with the service apikey.

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://postgres-auth:5432/auth_db` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `admin123` |
| `JWT_SECRET` | Secret key used to sign JWT tokens | `sportpulse-secret-key-2025` |
| `JWT_EXPIRATION` | Token expiry in milliseconds | `3600000` |

---

## Notes

- Passwords are never stored in plain text; bcrypt hashing is mandatory.
- The `GET /api/auth/validate` endpoint should be treated as internal and ideally not exposed through the gateway to end users.
- Token expiration is set to **3600 seconds (1 hour)** by default.
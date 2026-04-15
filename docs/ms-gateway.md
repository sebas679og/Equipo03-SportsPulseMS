# 🔵 ms-gateway — API Gateway

[← Back to README](../README.md)

## Overview

**Port:** `8080`

The `ms-gateway` is the single entry point for the entire SportPulse platform. It acts as a reverse proxy, redirecting every incoming request to the appropriate downstream microservice. It also enforces global rate limiting to protect the system from abuse.

No business logic lives here — its sole responsibilities are routing and traffic control.

---

## Routing Table

| Route Prefix | Destination |
|---|---|
| `/api/auth/**` | ms-auth:8081 |
| `/api/leagues/**` | ms-leagues:8082 |
| `/api/teams/**` | ms-teams:8083 |
| `/api/fixtures/**` | ms-fixtures:8085 |
| `/api/standings/**` | ms-standings:8086 |
| `/api/notifications/**` | ms-notifications:8088 |
| `/api/dashboard/**` | ms-dashboard:8089 |

---

## Endpoints

### 1. `ANY /api/**`

**Access:** Public (with restrictions)

**Description:**  

Acts as a reverse proxy, forwarding incoming requests to the corresponding microservice and returning its response without modification.

However, for security reasons, not all routes under `/api/**` are exposed to the client.

A set of **protected/internal routes** has been explicitly configured within the `filterChain`. Any request targeting these routes will be blocked and **will not be forwarded**, returning a `404 Not Found` response instead.

This prevents unintended exposure of internal communication endpoints between microservices.

Requests to routes **not included** in the protected configuration will be forwarded normally.

**Example of blocked response:**
```json
{
  "code": 404,
  "name": "NOT_FOUND",
  "description": "not found",
  "timestamp": "2025-01-15T10:30:00.000Z"
}
```

---

### 2. `GET /health`

**Access:** Public

**Description:** Returns the operational status of the gateway and all downstream services.

**Response `200 OK`:**
```json
{
  "gateway": "UP",
  "timestamp": "2025-01-15T10:30:00Z",
  "services": {
    "ms-auth": "UP",
    "ms-leagues": "UP",
    "ms-teams": "UP",
    "ms-fixtures": "UP",
    "ms-standings": "UP",
    "ms-notifications": "UP",
    "ms-dashboard": "UP"
  }
}
```

---

## 🔧 Rate Limiting

The system applies rate limiting based on the request type and endpoint.

### 🔹 Default Limit

**Limit:** 60 requests per minute per IP address  
**Refill:** Full refill every 60 seconds

All endpoints are protected by this limit unless a more restrictive rule is applied.

---

### 🔹 Brute Force Protection (`/login`)

**Limit:** 5 requests per minute per IP address  
**Refill:** Full refill every 60 seconds

To prevent brute force attacks, the `/login` endpoint has a stricter rate limit configuration.

---

### 🚫 Response when exceeded — `429 Too Many Requests`

```json
{
  "code": 429,
  "name": "TOO_MANY_REQUESTS",
  "description": "You have exceeded the rate limit. Please try again in 60 seconds.",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

### Notes:

- Rate limiting is applied per client IP.
- Limits are enforced using a token bucket strategy.
- When the limit is exceeded, requests are rejected until tokens are replenished.

---

## ⚠️ Service Unavailability Handling

**Status:** `503 Service Unavailable`

**Description:**  
When a downstream microservice is unavailable, unreachable, or fails to respond within the expected time, the gateway will not propagate the original error.

Instead, it returns a standardized `503 Service Unavailable` response to the client.

This ensures a consistent error contract and prevents exposing internal service details.

---

### 🚫 Response when service is unavailable

```json
{
  "code": 503,
  "name": "SERVICE_UNAVAILABLE",
  "description": "The service is temporarily unavailable. Please try again later.",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

### Notes:

- Applies to timeouts, connection failures, or unavailable downstream services.
- Prevents leaking internal infrastructure details.
- Encourages clients to implement retry mechanisms with backoff strategies.

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `SPORTS_PULSE_AUTH_SERVICE_URL` | URL for ms-auth | `http://ms-auth:8081` |
| `SPORTS_PULSE_LEAGUES_SERVICE_URL` | URL for ms-leagues | `http://ms-leagues:8082` |
| `SPORTS_PULSE_TEAMS_SERVICE_URL` | URL for ms-teams | `http://ms-teams:8083` |
| `SPORTS_PULSE_FIXTURES_SERVICE_URL` | URL for ms-fixtures | `http://ms-fixtures:8085` |
| `SPORTS_PULSE_STANDINGS_SERVICE_URL` | URL for ms-standings | `http://ms-standings:8086` |
| `SPORTS_PULSE_NOTIFICATIONS_SERVICE_URL` | URL for ms-notifications | `http://ms-notifications:8088` |
| `SPORTS_PULSE_DASHBOARD_SERVICE_URL` | URL for ms-dashboard | `http://ms-dashboard:8089` |
| `SPORTS_PULSE_REDIS_HOST` | Redis host | `localhost` |
| `SPORTS_PULSE_REDIS_PORT` | Redis port | `6379` |
| `SPORTS_PULSE_GATEWAY_CONNECT_TIMEOUT` | Gateway connection timeout (ms) | `3000` |
| `SPORTS_PULSE_GATEWAY_RESPONSE_TIMEOUT` | Gateway response timeout | `5s` |
| `SPORTS_PULSE_GATEWAY_ALLOWED_METHODS` | Allowed CORS HTTP methods | `GET,POST,PUT,DELETE` |
| `SPORTS_PULSE_GATEWAY_ALLOWED_ORIGINS` | Allowed CORS origins | `*` |
| `SPORTS_PULSE_GATEWAY_MAX_AGE` | CORS max age (seconds) | `3600` |
| `SPORTS_PULSE_LEVEL_LOGIN` | Logging level for Spring and root | `INFO` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `SWAGGER_UI_DOCUMENTATION_ENABLED` | Enable/disable Swagger/OpenAPI docs | `false` |

---

## Notes

- This service does **not** connect to any database.
- It does **not** validate JWT tokens — that responsibility belongs to each individual downstream service.
- The `/health` endpoint should actively ping each downstream service to determine its status, not just return a static response.
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

**Access:** Public (forwards to the corresponding service)

**Description:** Reverse proxy. Redirects the request and returns the destination service's response without modification.

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

**Limit:** 60 requests/minute per IP address.

**Response when exceeded — `429 Too Many Requests`:**
```json
{
  "code": 429,
  "name": "TOO_MANY_REQUESTS",
  "description": "You have exceeded the rate limit. Please try again later.",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `AUTH_SERVICE_URL` | URL for ms-auth | `http://ms-auth:8081` |
| `LEAGUES_SERVICE_URL` | URL for ms-leagues | `http://ms-leagues:8082` |
| `TEAMS_SERVICE_URL` | URL for ms-teams | `http://ms-teams:8083` |
| `FIXTURES_SERVICE_URL` | URL for ms-fixtures | `http://ms-fixtures:8085` |
| `STANDINGS_SERVICE_URL` | URL for ms-standings | `http://ms-standings:8086` |
| `NOTIFICATIONS_SERVICE_URL` | URL for ms-notifications | `http://ms-notifications:8088` |
| `DASHBOARD_SERVICE_URL` | URL for ms-dashboard | `http://ms-dashboard:8089` |

---

## Notes

- This service does **not** connect to any database.
- It does **not** validate JWT tokens — that responsibility belongs to each individual downstream service.
- The `/health` endpoint should actively ping each downstream service to determine its status, not just return a static response.
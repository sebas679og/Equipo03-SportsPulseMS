# 🔴 ms-teams — Teams Service

[← Back to README](../README.md)

## Overview

**Port:** `8083`  
**Database:** None (data sourced from API-Football)  
**External API:** `GET /teams` from API-Football

The `ms-teams` service manages football team information including team name, crest, country of origin, founding year, and stadium details. It is a **core dependency** for several other services — `ms-fixtures`, `ms-standings`, and `ms-dashboard` all call this service to enrich their own responses with team data.

---

## Authentication

All endpoints require a valid JWT token issued by `ms-auth`, whether the request comes from an end user or from another internal microservice.

**Required Header:**
```
Authorization: Bearer <token> (must be a valid JWT)
X-Internal-API-Key: <internal_api_key> (for internal calls only)
```

---

## Endpoints

### 1. `GET /api/teams`

**Access:** Authenticated USER

**Description:** Returns a list of football teams filtered by league and season. Both parameters are required.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `league` | int | Yes | League ID (e.g. `140` for La Liga) |
| `season` | int | Yes | Season year (e.g. `2024`) |

**Response `200 OK`:**
```json
[
  {
    "id": 529,
    "name": "FC Barcelona",
    "country": "Spain",
    "logo": "https://media.api-sports.io/football/teams/529.png",
    "founded": 1899,
    "stadium": {
      "name": "Camp Nou",
      "city": "Barcelona",
      "capacity": 99354
    }
  },
  {
    "id": 541,
    "name": "Real Madrid",
    "country": "Spain",
    "logo": "https://media.api-sports.io/football/teams/541.png",
    "founded": 1902,
    "stadium": {
      "name": "Estadio Santiago Bernabéu",
      "city": "Madrid",
      "capacity": 81044
    }
  }
]
```

---

### 2. `GET /api/teams/{teamId}`

**Access:** Authenticated USER + internal calls from other microservices

**Description:** Returns the full details of a single team by its ID. This endpoint is used internally by `ms-standings`, `ms-fixtures`, and `ms-dashboard` to enrich their data with complete team information.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `teamId` | int | The API-Football team ID |

**Response `200 OK`:**
```json
{
  "id": 529,
  "name": "FC Barcelona",
  "country": "Spain",
  "logo": "https://media.api-sports.io/football/teams/529.png",
  "founded": 1899,
  "national": false,
  "stadium": {
    "name": "Camp Nou",
    "address": "C/ d'Arístides Maillol, s/n",
    "city": "Barcelona",
    "capacity": 99354,
    "surface": "grass"
  }
}
```

**Response `404 Not Found`:**
```json
{
  "code": 404,
  "name": "Not Found",
  "description": "No team found with the provided ID",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

---

## Caching Strategy

Since this service is called by multiple other services, implementing caching is critical to avoid hitting the API-Football rate limit.

- Cache individual team responses by `teamId`.
- Cache team lists by `league` + `season` combination.
- Recommended TTL: several hours, as team data changes infrequently.

---

## Environment Variables

| Variable                               | Description                                            | Example                             |
| -------------------------------------- | ------------------------------------------------------ | ----------------------------------- |
| `SPRING_PROFILES_ACTIVE`               | Active Spring profile (e.g. dev, prod)                 | `prod`                              |
| `SPORTS_PULSE_LEVEL_LOGIN`             | Logging level for the application                      | `INFO`                              |
| `SWAGGER_UI_DOCUMENTATION_ENABLED`     | Enables or disables Swagger/OpenAPI documentation      | `false`                             |
| `SPORTS_PULSE_API_FOOTBALL_BASE_URL`   | Base endpoint for the Api-Football service             | `https://v3.football.api-sports.io` |
| `SPORTS_PULSE_API_FOOTBALL_KEY`        | API key for Api-Football (RapidAPI)                    | `your_api_key_here`                 |
| `SPORTS_PULSE_AUTH_SERVICE_URL`        | Base URL for the authentication service                | `http://localhost:8080`             |
| `SPORTS_PULSE_INTERNAL_API_KEY`        | Internal API key for service-to-service authentication | `internal-service-key`              |
| `SPORTS_PULSE_CACHE_TEAMS_ENABLED`     | Enables or disables caching for teams                  | `true`                              |
| `SPORTS_PULSE_CACHE_TEAMS_TTL_MINUTES` | Cache time-to-live in minutes                          | `60`                                |
| `SPORTS_PULSE_CACHE_TEAMS_MAX_SIZE`    | Maximum number of entries in cache                     | `1000`                              |
| `JWT_SECRET`                           | Shared JWT secret for token validation                 | `sportpulse-secret-key-2025`        |


---

## Notes

- This service does **not** use a database. All data is fetched from API-Football and cached in memory.
- Because it is consumed by other services, its availability is critical. Consider implementing fallback/circuit-breaker patterns (e.g. Resilience4j) for robustness.
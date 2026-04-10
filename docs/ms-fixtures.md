# 🟡 ms-fixtures — Fixtures Service

[← Back to README](../README.md)

## Overview

**Port:** `8085`  
**Database:** None (data sourced from API-Football)  
**External API:** `GET /fixtures` from API-Football  
**Depends on:** ms-teams (for team data enrichment)

The `ms-fixtures` service manages football match data: upcoming fixtures, live matches, and past results. It consumes `ms-teams` to enrich each match's home and away team information. It is also consumed by `ms-notifications` and `ms-dashboard`.

---

## Authentication

All endpoints require a valid JWT token issued by `ms-auth`.

**Required Header:**
```
Authorization: Bearer <token>
```

---

## Endpoints

### 1. `GET /api/fixtures`

**Access:** Authenticated USER

**Description:** Returns a list of matches filtered by one or more optional query parameters. At least one filter is recommended for meaningful results.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `league` | int | No | Filter by league ID |
| `team` | int | No | Filter by team ID |
| `date` | string | No | Filter by date — format `YYYY-MM-DD` |
| `status` | string | No | Filter by status: `NS` (not started), `LIVE`, `FT` (finished) |

**Response `200 OK`:**
```json
[
  {
    "id": 1035065,
    "date": "2025-01-20T21:00:00+01:00",
    "status": { "short": "NS", "long": "Not Started" },
    "league": { "id": 140, "name": "La Liga", "round": "Matchday 21" },
    "homeTeam": {
      "id": 529,
      "name": "FC Barcelona",
      "logo": "https://media.api-sports.io/football/teams/529.png",
      "goals": null
    },
    "awayTeam": {
      "id": 541,
      "name": "Real Madrid",
      "logo": "https://media.api-sports.io/football/teams/541.png",
      "goals": null
    },
    "venue": { "name": "Camp Nou", "city": "Barcelona" }
  }
]
```

---

### 2. `GET /api/fixtures/live`

**Access:** Authenticated USER

**Description:** Returns all currently ongoing matches, including the elapsed minute.

**Response `200 OK`:**
```json
[
  {
    "id": 1035020,
    "elapsed": 67,
    "status": { "short": "2H", "long": "Second Half" },
    "league": { "id": 39, "name": "Premier League" },
    "homeTeam": { "id": 33, "name": "Manchester United", "goals": 2 },
    "awayTeam": { "id": 40, "name": "Liverpool", "goals": 1 }
  }
]
```

---

### 3. `GET /api/fixtures/{fixtureId}/events`

**Access:** Authenticated USER

**Description:** Returns all events (goals, cards, substitutions) for a specific match.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `fixtureId` | int | The API-Football fixture ID |

**Response `200 OK`:**
```json
[
  {
    "elapsed": 23,
    "type": "Goal",
    "detail": "Normal Goal",
    "team": { "id": 529, "name": "FC Barcelona" },
    "player": { "id": 1100, "name": "Robert Lewandowski" },
    "assist": { "id": 284, "name": "Pedri" }
  },
  {
    "elapsed": 55,
    "type": "Card",
    "detail": "Yellow Card",
    "team": { "id": 541, "name": "Real Madrid" },
    "player": { "id": 276, "name": "Kylian Mbappé" },
    "assist": null
  }
]
```

**Response `404 Not Found`:**
```json
{
  "code": 404,
  "name": "Not Found",
  "description": "No match found with the provided ID",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

---

## Inter-Service Communication

This service calls `ms-teams` to enrich team data in fixture responses:

```java
@FeignClient(name = "ms-teams", url = "${teams.service.url}")
public interface TeamClient {

    @GetMapping("/api/teams/{teamId}")
    TeamResponse getTeam(@PathVariable Integer teamId);
}
```

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `RAPIDAPI_KEY` | API key for RapidAPI | `your_api_key_here` |
| `JWT_SECRET` | Shared JWT secret for token validation | `sportpulse-secret-key-2025` |
| `TEAMS_SERVICE_URL` | URL for ms-teams | `http://ms-teams:8083` |

---

## Notes

- This service does **not** use a database.
- It is consumed by `ms-notifications` (to detect match events) and `ms-dashboard` (for today's matches summary).
- The `/live` endpoint should be treated as high-frequency and cached with a short TTL (e.g. 30–60 seconds) to avoid burning API quota.
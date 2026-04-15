# 🔵 ms-standings — Standings Service

[← Back to README](../README.md)

## Overview

**Port:** `8086`  
**Database:** None (data sourced from API-Football)  
**External API:** `GET /standings` from API-Football  
**Depends on:** ms-teams (for team data enrichment)

The `ms-standings` service generates and exposes the up-to-date classification table for any league and season. It consumes `ms-teams` to enrich each team entry in the standings with full team details (logo, stadium, etc.).

---

## Authentication

All endpoints require a valid JWT token issued by `ms-auth`.

**Required Header:**
```
Authorization: Bearer <token>
```

---

## Endpoints

### 1. `GET /api/standings`

**Access:** Authenticated USER

**Description:** Returns the full standings table for a given league and season.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `league` | int | Yes | League ID (e.g. `140` for La Liga) |
| `season` | int | Yes | Season year (e.g. `2024`) |

**Response `200 OK`:**
```json
{
  "league": {
    "id": 140,
    "name": "La Liga",
    "country": "Spain",
    "season": 2024
  },
  "standings": [
    {
      "rank": 1,
      "team": {
        "id": 529,
        "name": "FC Barcelona",
        "logo": "https://media.api-sports.io/football/teams/529.png"
      },
      "points": 48,
      "played": 20,
      "won": 15,
      "drawn": 3,
      "lost": 2,
      "goalsFor": 52,
      "goalsAgainst": 18,
      "goalDifference": 34,
      "form": "WWWDW"
    },
    {
      "rank": 2,
      "team": {
        "id": 541,
        "name": "Real Madrid",
        "logo": "https://media.api-sports.io/football/teams/541.png"
      },
      "points": 44,
      "played": 20,
      "won": 13,
      "drawn": 5,
      "lost": 2,
      "goalsFor": 44,
      "goalsAgainst": 20,
      "goalDifference": 24,
      "form": "WDWWW"
    }
  ]
}
```

---

### 2. `GET /api/standings/team/{teamId}`

**Access:** Authenticated USER

**Description:** Returns the specific position of a single team within the standings for a given league and season.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `teamId` | int | The API-Football team ID |

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `league` | int | Yes | League ID |
| `season` | int | Yes | Season year |

**Response `200 OK`:**
```json
{
  "team": { "id": 529, "name": "FC Barcelona" },
  "league": { "id": 140, "name": "La Liga" },
  "season": 2024,
  "rank": 1,
  "points": 48,
  "played": 20,
  "form": "WWWDW",
  "description": "Promotion - Champions League (Group Stage)"
}
```

---

## Inter-Service Communication

This service calls `ms-teams` to enrich team data in standings responses:

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
| `SPORT_PULSE_API_BASE_URL` | Base endpoint for the football API     | `https://v3.football.api-sports.io` |

---

## Notes

- This service does **not** use a database.
- Standings data changes after each match, so caching TTL should be aligned with the league's match frequency (e.g. refresh every few hours on match days).
- It is consumed by `ms-dashboard` for the standings preview section of the daily summary.
# 🟤 ms-dashboard — Dashboard Service

[← Back to README](../README.md)

## Overview

**Port:** `8089`  
**Database:** None  
**Depends on:** ms-fixtures, ms-standings, ms-leagues

The `ms-dashboard` service acts as an **aggregator**. It does not call API-Football directly. Instead, it fetches data from three other internal microservices — `ms-fixtures`, `ms-standings`, and `ms-leagues` — and combines the results into a single, unified executive summary for a given league and day.

This is the service a frontend dashboard would call to populate an overview screen.

---

## Authentication

All endpoints require a valid JWT token issued by `ms-auth`.

**Required Header:**
```
Authorization: Bearer <token>
```

---

## Endpoints

### 1. `GET /api/dashboard`

**Access:** Authenticated USER

**Description:** Returns the main daily summary for a league: today's matches, the top 3 teams in the standings, and the top 3 goal scorers of the season.

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
    "country": "Spain"
  },
  "today": "2025-01-20",
  "matchesToday": [
    {
      "id": 1035065,
      "date": "2025-01-20T21:00:00+01:00",
      "homeTeam": { "name": "FC Barcelona", "logo": "..." },
      "awayTeam": { "name": "Real Madrid", "logo": "..." },
      "status": "NS"
    }
  ],
  "standingsPreview": [
    { "rank": 1, "team": "FC Barcelona", "points": 48, "played": 20 },
    { "rank": 2, "team": "Real Madrid", "points": 44, "played": 20 },
    { "rank": 3, "team": "Atlético Madrid", "points": 38, "played": 20 }
  ],
  "topScorers": [
    { "rank": 1, "name": "Kylian Mbappé", "team": "Real Madrid", "goals": 18 },
    { "rank": 2, "name": "Robert Lewandowski", "team": "FC Barcelona", "goals": 15 },
    { "rank": 3, "name": "Antoine Griezmann", "team": "Atlético Madrid", "goals": 12 }
  ],
  "lastUpdated": "2025-01-20T09:00:00Z"
}
```

**Response fields:**

| Field | Source Service | Description |
|---|---|---|
| `league` | ms-leagues | League name and country |
| `matchesToday` | ms-fixtures | Fixtures filtered by today's date |
| `standingsPreview` | ms-standings | Top 3 teams in the current standings |
| `topScorers` | API-Football via ms-standings or direct | Top 3 scorers in the league season |
| `lastUpdated` | ms-dashboard | Timestamp of last data aggregation |

---

## Inter-Service Communication

The dashboard orchestrates calls to three services concurrently:

```
Client
  │
  └─► ms-dashboard
          │
          ├─► GET /api/fixtures?league={id}&date={today}   → ms-fixtures
          ├─► GET /api/standings?league={id}&season={year} → ms-standings
          └─► GET /leagues/{id}                            → ms-leagues
```

**Feign Client — ms-fixtures:**
```java
@FeignClient(name = "ms-fixtures", url = "${fixtures.service.url}")
public interface FixtureClient {

    @GetMapping("/api/fixtures")
    List<FixtureResponse> getFixturesByDate(
        @RequestParam Integer league,
        @RequestParam String date
    );
}
```

**Feign Client — ms-standings:**
```java
@FeignClient(name = "ms-standings", url = "${standings.service.url}")
public interface StandingsClient {

    @GetMapping("/api/standings")
    StandingsResponse getStandings(
        @RequestParam Integer league,
        @RequestParam Integer season
    );
}
```

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `JWT_SECRET` | Shared JWT secret for token validation | `sportpulse-secret-key-2025` |
| `FIXTURES_SERVICE_URL` | URL for ms-fixtures | `http://ms-fixtures:8085` |
| `STANDINGS_SERVICE_URL` | URL for ms-standings | `http://ms-standings:8086` |
| `LEAGUES_SERVICE_URL` | URL for ms-leagues | `http://ms-leagues:8082` |
| `SPORT_PULSE_API_BASE_URL` | Base endpoint for the football API | `https://v3.football.api-sports.io` |
| `FOOTBALL_API_KEY`             | Private key for API-Football authentication | `YOUR_API_KEY`|

---

## Notes

- This service does not use a database. It calls API-Football directly only for the topScorers endpoint.
- All data aggregation happens at request time. Consider caching the dashboard response for a few minutes (e.g. 5–10 min TTL) to reduce load on downstream services.
- To improve performance, downstream calls can be made in **parallel** using `CompletableFuture` or reactive streams.
- The `topScorers` data is fetched directly from `GET /players/topscorers` using the centralized `SPORT_PULSE_API_BASE_URL` configuration.
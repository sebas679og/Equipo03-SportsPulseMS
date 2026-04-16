# 🟢 ms-leagues — Leagues Service

[← Back to README](../README.md)

## Overview

**Port:** `8082`  
**Database:** None (data sourced from API-Football)  
**External API:** `GET /leagues` from API-Football

The `ms-leagues` service exposes information about football leagues, countries, and seasons. All data is fetched from the **API-Football** external API. In-memory caching must be implemented to minimise the number of requests made to the external API, especially during development (free plan limit: 100 requests/day).

---

## Authentication

All endpoints in this service require a valid JWT token issued by `ms-auth`.

**Required Header:**
```
Authorization: Bearer <token>
```

Tokens can be validated locally using the shared `JWT_SECRET` or by calling `POST /api/auth/validate` on `ms-auth`.

---

## Endpoints

### 1. `GET /api/leagues`

**Access:** Authenticated USER

**Description:** Returns a list of available football leagues. Results can be filtered by country or season using optional query parameters.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `country` | string | No | Filter by country name (e.g. `Spain`) |
| `season` | int | No | Filter by season year (e.g. `2024`) |

**Response `200 OK`:**
```json
[
  {
    "id": 140,
    "name": "La Liga",
    "type": "League",
    "country": "Spain",
    "logo": "https://media.api-sports.io/football/leagues/140.png",
    "currentSeason": 2024,
    "startDate": "2024-08-17",
    "endDate": "2025-05-25"
  },
  {
    "id": 39,
    "name": "Premier League",
    "type": "League",
    "country": "England",
    "logo": "https://media.api-sports.io/football/leagues/39.png",
    "currentSeason": 2024,
    "startDate": "2024-08-16",
    "endDate": "2025-05-25"
  }
]
```

---

### 2. `GET /api/leagues/{leagueId}`

**Access:** Authenticated USER

**Description:** Returns the full details of a specific league identified by its ID, including all available seasons and the current season's information.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `leagueId` | int | The API-Football league ID |

**Response `200 OK`:**
```json
{
  "id": 140,
  "name": "La Liga",
  "type": "League",
  "country": "Spain",
  "logo": "https://media.api-sports.io/football/leagues/140.png",
  "seasons": [2021, 2022, 2023, 2024],
  "currentSeason": {
    "year": 2024,
    "startDate": "2024-08-17",
    "endDate": "2025-05-25",
    "current": true
  }
}
```

**Response `404 Not Found`:**
```json
{
  "code": 404,
  "name": "Not Found",
  "description": "No league found with the provided ID",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

---

## Caching Strategy

Since the free API-Football plan allows only **100 requests per day**, in-memory caching is essential. Recommended approach:

- Use Spring's `@Cacheable` annotation with `ConcurrentMapCacheManager` or [Caffeine](https://github.com/ben-manes/caffeine).
- Cache the list of leagues (it rarely changes) with a TTL of several hours.
- Cache individual league details by `leagueId`.

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `FOOTBALL_API_KEY` | API key for RapidAPI | `your_api_key_here` |
| `JWT_SECRET` | Shared JWT secret for token validation | `sportpulse-secret-key-2025` |
| `SPORT_PULSE_API_BASE_URL` | Base endpoint for the football API     | `https://v3.football.api-sports.io` |

---

## Notes

- This service does **not** use a database. All data is fetched from the external API and cached in memory.
- The service is consumed by `ms-dashboard` to enrich the daily summary.
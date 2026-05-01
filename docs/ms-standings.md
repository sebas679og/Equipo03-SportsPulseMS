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

## Environment Variables

| Variable                                   | Valor por defecto | Descripción                       |
| ------------------------------------------ | ----------------- | --------------------------------- |
| `SPRING_PROFILES_ACTIVE`                   | `prod`            | Perfil activo de Spring           |
| `SPORTS_PULSE_LEVEL_LOGIN`                 | `INFO`            | Nivel de logging (Spring y root)  |
| `SWAGGER_UI_DOCUMENTATION_ENABLED`         | `false`           | Habilita Swagger UI y API docs    |
| `SPORTS_PULSE_API_FOOTBALL_BASE_URL`       | —                 | URL base de la API de fútbol      |
| `SPORTS_PULSE_API_FOOTBALL_KEY`            | —                 | API Key de ApiFootball            |
| `SPORTS_PULSE_AUTH_SERVICE_URL`            | —                 | URL del servicio de autenticación |
| `SPORTS_PULSE_INTERNAL_API_KEY`            | —                 | API Key interna                   |
| `SPORTS_PULSE_CACHE_STANDINGS_ENABLED`     | `true`            | Habilita caché                    |
| `SPORTS_PULSE_CACHE_STANDINGS_TTL_MINUTES` | `1440`            | Tiempo de vida del caché          |
| `SPORTS_PULSE_CACHE_STANDINGS_MAX_SIZE`    | `1000`            | Tamaño máximo del caché           |


---

## Notes

- This service does **not** use a database.
- Standings data changes after each match, so caching TTL should be aligned with the league's match frequency (e.g. refresh every few hours on match days).
- It is consumed by `ms-dashboard` for the standings preview section of the daily summary.
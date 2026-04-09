# 🔔 ms-notifications — Notifications Service

[← Back to README](../README.md)

## Overview

**Port:** `8088`  
**Database:** PostgreSQL (`notifications_db`)  
**Depends on:** ms-fixtures (to detect match changes)

The `ms-notifications` service allows authenticated users to subscribe to football event alerts for specific teams or matches. When a subscribed event occurs (goal, match start, etc.), the service simulates delivering a notification via the configured channel — currently **WEBHOOK** (HTTP POST to a user-provided URL) and **LOG** (logs the event internally for development/testing purposes).

---

## Data Model

### Entity: `Subscription`

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Unique identifier |
| `userId` | UUID | ID of the subscribed user |
| `type` | Enum | Subscription type |
| `teamId` | Integer | Team ID (when type = `TEAM`) |
| `fixtureId` | Integer | Fixture ID (when type = `FIXTURE`) |
| `events` | List\<Enum\> | List of events to be notified about |
| `channel` | Enum | Delivery channel |
| `webhookUrl` | String | Destination URL for webhook delivery |
| `status` | Enum | Current status of the subscription |
| `createdAt` | Instant | Creation timestamp |

### Enums

```
SubscriptionType:    TEAM, FIXTURE
NotificationEvent:   MATCH_START, GOAL, MATCH_END, CARD
NotificationChannel: WEBHOOK, LOG
SubscriptionStatus:  ACTIVE, CANCELLED
```

---

## Authentication

All endpoints require a valid JWT token issued by `ms-auth`.

**Required Header:**
```
Authorization: Bearer <token>
```

---

## Endpoints

### 1. `POST /api/notifications/subscribe`

**Access:** Authenticated USER

**Description:** Creates an alert subscription for a team or a specific match.

**Request Body:**
```json
{
  "type": "TEAM",
  "teamId": 529,
  "events": ["MATCH_START", "GOAL", "MATCH_END"],
  "channel": "WEBHOOK",
  "webhookUrl": "https://myapp.com/webhook/football"
}
```

**Field Notes:**
- `type`: Use `TEAM` to subscribe to all matches involving a team. Use `FIXTURE` for a specific match.
- `teamId`: Required when `type` is `TEAM`.
- `fixtureId`: Required when `type` is `FIXTURE`.
- `webhookUrl`: Required when `channel` is `WEBHOOK`.

**Response `201 Created`:**
```json
{
  "subscriptionId": "770e8400-e29b-41d4-a716-446655440010",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "type": "TEAM",
  "teamId": 529,
  "events": ["MATCH_START", "GOAL", "MATCH_END"],
  "channel": "WEBHOOK",
  "status": "ACTIVE",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

---

### 2. `GET /api/notifications/subscriptions`

**Access:** Authenticated USER

**Description:** Returns all active subscriptions belonging to the currently authenticated user. The user is identified from the JWT token.

**Response `200 OK`:**
```json
[
  {
    "subscriptionId": "770e8400-e29b-41d4-a716-446655440010",
    "type": "TEAM",
    "teamId": 529,
    "events": ["MATCH_START", "GOAL", "MATCH_END"],
    "channel": "WEBHOOK",
    "webhookUrl": "https://myapp.com/webhook/football",
    "status": "ACTIVE"
  }
]
```

---

### 3. `DELETE /api/notifications/subscribe/{subscriptionId}`

**Access:** Authenticated USER

**Description:** Cancels an existing subscription. Only the owner of the subscription can cancel it.

**Path Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `subscriptionId` | UUID | The subscription ID to cancel |

**Response `200 OK`:**
```json
{
  "subscriptionId": "770e8400-e29b-41d4-a716-446655440010",
  "status": "CANCELLED",
  "cancelledAt": "2025-01-20T10:30:00Z"
}
```

---

## Inter-Service Communication

This service calls `ms-fixtures` to poll for match events and detect changes:

```java
@FeignClient(name = "ms-fixtures", url = "${fixtures.service.url}")
public interface FixtureClient {

    @GetMapping("/api/fixtures/live")
    List<LiveFixtureResponse> getLiveFixtures();
}
```

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://postgres-notifications:5432/notifications_db` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `admin123` |
| `JWT_SECRET` | Shared JWT secret for token validation | `sportpulse-secret-key-2025` |
| `FIXTURES_SERVICE_URL` | URL for ms-fixtures | `http://ms-fixtures:8085` |

---

## Notes

- This is one of only two services (alongside `ms-auth`) that uses a **persistent database**, as subscriptions need to survive service restarts.
- Webhook delivery is **simulated** — the service sends HTTP POST requests to the `webhookUrl` but does not implement retry logic in the base version.
- For development and testing, use `channel: "LOG"` to avoid needing a real webhook endpoint.
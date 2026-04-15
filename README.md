# ⚽ SportPulse — Football Microservices Platform

SportPulse is a real-time football analysis platform built with a microservices architecture. It consists of **8 independent microservices** that communicate with each other and consume data from the **API-Football** external API via RapidAPI.

Each team must design, implement, document, and deploy the complete system in their own repository. By the end of the sprint, all teams will have independently built the same platform.

---

## 📋 Project Overview

| Field | Details |
|---|---|
| **Project Name** | SportPulse — Football Analysis Platform |
| **Type** | Backend with microservices architecture connected to an external API |
| **External API** | API-Football (RapidAPI) — https://rapidapi.com/apisports/api/api-football |
| **Duration** | 2 weeks (1 sprint) |
| **Mode** | Each team builds the complete system independently |

---

## 🛠️ Technologies & Tools

| Category | Technology                                                  |
|---|-------------------------------------------------------------|
| **Language** | Java 21                                                     |
| **Framework** | Spring Boot 3.5.x                                           |
| **Security** | Spring Security + JWT                                       |
| **Database** | PostgreSQL (one DB per microservice, only where applicable) |
| **External API** | API-Football via RapidAPI                                   |
| **Inter-service Communication** | OpenFeign / RestTemplate                                    |
| **Documentation** | Swagger UI / OpenAPI 3.1                                    |
| **Testing** | JUnit 5 + Mockito + Postman                                 |
| **Mappers** | MapStruct                                                   |
| **Utilities** | Lombok                                                      |
| **Containerization** | Docker + Docker Compose                                     |
| **Build Tool** | Maven or Gradle                                             |

---

## 🧩 Microservices

| Microservice | Port | Responsibility |
|---|---|---|
| [ms-gateway](./docs/ms-gateway.md) | 8080 | Entry point, routing and rate limiting |
| [ms-auth](./docs/ms-auth.md) | 8081 | Registration, login and JWT token issuance |
| [ms-leagues](./docs/ms-leagues.md) | 8082 | Leagues, countries and seasons |
| [ms-teams](./docs/ms-teams.md) | 8083 | Teams, crests and general info |
| [ms-fixtures](./docs/ms-fixtures.md) | 8085 | Matches, schedules and results |
| [ms-standings](./docs/ms-standings.md) | 8086 | League standings by season |
| [ms-notifications](./docs/ms-notifications.md) | 8088 | Subscriptions and event alerts |
| [ms-dashboard](./docs/ms-dashboard.md) | 8089 | Aggregated executive summary |

---

## 🔗 Inter-Service Communication

```
ms-gateway          → routes to all services
ms-dashboard        → consumes: ms-fixtures, ms-standings, ms-leagues
ms-standings        → consumes: ms-teams (to enrich standings data)
ms-fixtures         → consumes: ms-teams
ms-notifications    → consumes: ms-fixtures
All domain services → validate JWT issued by ms-auth
```

---

## 🔑 External API — API-Football (RapidAPI)

### **Configuration Details:**

- **Base URL:** Managed via `SPORT_PULSE_API_BASE_URL` (`https://v3.football.api-sports.io`)

- **Authentication:** Managed via `RAPIDAPI_KEY` (Injected as `x-apisports-Key` header).

### ⚙️ **Environment Variables**

| Variable                   | Description | Value                         |
|----------------------------|-------------|-------------------------------|
| `SPORT_PULSE_API_BASE_URL` | Base endpoint for the football API | `https://v3.football.api-sports.io` |
| `RAPIDAPI_KEY`             | Your private secret key from RapidAPI | `YOUR_API_KEY`|

### **Endpoint Mapping**

The following endpoints are consumed by the microservices using the `base-url` defined above:

| RapidAPI Endpoint | Used by |
|---|---|
| `GET /leagues` | ms-leagues |
| `GET /teams` | ms-teams |
| `GET /fixtures` | ms-fixtures |
| `GET /standings` | ms-standings |
| `GET /players/topscorers` | ms-dashboard |

> ⚠️ The free plan allows **100 requests/day**. Implement in-memory caching to avoid exhausting the limit during development.

---

## 🐳 Running the Project

### Prerequisites

- Docker & Docker Compose installed
- A valid RapidAPI key for API-Football

### Setup

1. Clone the repository:
```bash
git https://github.com/sebas679og/Equipo03-SportsPulseMS
cd Equipo03-SportsPulseMS
```

2. Copy the template and edite the `.env` file:

```bash
cp .env.template .env
```

> [!IMPORTANT]
> Do **not** commit this file. Add `.env` to your `.gitignore`.

3. Start all services:
```bash
docker-compose up --build
```

### Service URLs (local)

| Service | URL |
|---|---|
| Gateway | http://localhost:8080 |
| Auth | http://localhost:8081 |
| Leagues | http://localhost:8082 |
| Teams | http://localhost:8083 |
| Fixtures | http://localhost:8085 |
| Standings | http://localhost:8086 |
| Notifications | http://localhost:8088 |
| Dashboard | http://localhost:8089 |

### Swagger UI

Each service exposes its API documentation at:
```
http://localhost:{PORT}/swagger-ui/index.html
```

---

## 📦 Deliverables

- [ ] Source code for all 8 microservices in the team's GitHub repository
- [ ] Swagger/OpenAPI documentation accessible at `/swagger-ui` for each service
- [ ] Postman collection with all system endpoints
- [ ] Functional `Dockerfile` + `docker-compose.yml` to spin up the entire system
- [ ] `README.md` with installation instructions, environment variables and usage examples
- [ ] Minimum **3 tests** per microservice (unit or integration)
- [ ] **5-minute live demo** showing the complete system in operation

---

## 📁 Documentation

| Microservice | Description |
|---|---|
| [ms-gateway](./docs/ms-gateway.md) | API gateway, reverse proxy, rate limiting |
| [ms-auth](./docs/ms-auth.md) | Authentication, JWT issuance and validation |
| [ms-leagues](./docs/ms-leagues.md) | Football leagues and seasons |
| [ms-teams](./docs/ms-teams.md) | Football teams and stadiums |
| [ms-fixtures](./docs/ms-fixtures.md) | Matches, live scores and events |
| [ms-standings](./docs/ms-standings.md) | League classification tables |
| [ms-notifications](./docs/ms-notifications.md) | Alert subscriptions and webhooks |
| [ms-dashboard](./docs/ms-dashboard.md) | Aggregated daily summary |

---

## Contribution

See [CONTRIBUTING.md](./CONTRIBUTING.md) to learn about the workflow, branch conventions, commits, and pull requests.
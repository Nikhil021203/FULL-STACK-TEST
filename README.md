# Full-Stack Test Drive

A deliberately small, one-page learning laboratory. It demonstrates the complete path from a **Next.js/React/TypeScript** browser interface to a **Java/Spring Boot REST API**, then through **Spring Data JPA + Hibernate** to **PostgreSQL**. It also contains a small **Spring Security** login and protected endpoint.

It is designed for observation and learning, not production. In particular, CSRF is disabled so the separate local frontend and backend are easy to call. Re-enable it and add production-grade user storage and HTTPS for real applications.

## Layout

```
FULL-STACK-TEST/
├── frontend/                     # Next.js, React, TypeScript, HTML, CSS
│   ├── app/page.tsx               # One dashboard page and browser REST calls
│   └── app/globals.css            # Dashboard styling
├── backend/                      # Java and Spring Boot REST API
│   └── src/main/java/com/example/fullstacktest/
│       ├── api/ApiController.java          # status, hello, auth, protected APIs
│       ├── item/                           # JPA entity, repository, CRUD controller
│       └── config/SecurityConfig.java      # security, CORS and demo user
├── docker-compose.yml             # Optional PostgreSQL setup
├── Dockerfile                     # Render production build: Next.js → Spring Boot JAR
├── render.yaml                    # Render Blueprint: web service + PostgreSQL
└── README.md
```

## Stack map

| Technology | Location | What it demonstrates |
| --- | --- | --- |
| Next.js | `frontend/` | Development server and application framework on port 3000. |
| React + TypeScript | `frontend/app/page.tsx` | Typed, interactive dashboard components. |
| HTML + CSS | JSX and `globals.css` | Semantic page structure and visual styling. |
| Java + Spring Boot | `backend/` | Web server and REST API on port 8080. |
| REST API | `ApiController`, `LabItemController` | HTTP boundary between two separate applications. |
| PostgreSQL | local database / Docker | Permanent storage for the lab records. |
| Spring Data JPA + Hibernate | `LabItem`, `LabItemRepository` | Object mapping and database CRUD without hand-written SQL. |
| Spring Security | `SecurityConfig` | Login, logout, session cookie, and an authenticated endpoint. |
| Git + GitHub | repository root | Version the complete folder and publish it to GitHub. |

## Request flow

```
Next.js + React browser dashboard (port 3000)
       │ fetch() sends JSON / session cookie
       ▼
Spring Boot REST controllers (port 8080)
       │ JpaRepository CRUD calls
       ▼
Spring Data JPA → Hibernate → generated SQL
       ▼
PostgreSQL
```

The React page calls the backend only through HTTP. It never connects directly to PostgreSQL. For login, it posts form data to Spring Security; Spring creates a server-side session and returns a cookie. The frontend uses `credentials: "include"` so later requests include that cookie. The protected endpoint checks the session before responding.

## Prerequisites

- Java 25 and Maven 3.9+
- Node.js 20+ and npm
- PostgreSQL 16+ **or** Docker Desktop

## 1. Configure and start PostgreSQL

### Docker option

From `FULL-STACK-TEST/` run:

```bash
docker compose up -d
```

It creates a database at `localhost:5432` with database `full_stack_test`, username `postgres`, and password `postgres`.

### Existing PostgreSQL option

Create a database named `full_stack_test`. If your values differ from the defaults in `backend/src/main/resources/application.yml`, set these before starting the backend:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/full_stack_test"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-password"
```

`spring.jpa.hibernate.ddl-auto: update` lets Hibernate create/update the `lab_items` table for this teaching project. `show-sql: true` displays generated SQL in the backend terminal.

## 2. Start Spring Boot

In a terminal at `backend/`:

```bash
mvn spring-boot:run
```

The REST API will be available at `http://localhost:8080`.

## 3. Start Next.js

In a second terminal at `frontend/`:

```bash
npm install
npm run dev
```

Visit `http://localhost:3000`. The frontend expects the API at `http://localhost:8080`. To change that, copy `.env.local.example` to `.env.local` and edit `NEXT_PUBLIC_API_URL`.

## Endpoints

| Endpoint | Purpose |
| --- | --- |
| `GET /api/status` | Confirms Spring Boot and runs a database count query. |
| `GET /api/hello` | Simple Java/Spring REST response. |
| `GET /api/items` | Read the PostgreSQL records. |
| `POST /api/items` | Create `{ "title": "...", "note": "..." }`. |
| `PUT /api/items/{id}` | Update a record. |
| `DELETE /api/items/{id}` | Delete a record. |
| `POST /api/auth/login` | Spring Security login using form fields `username`, `password`. |
| `POST /api/auth/logout` | End the session. |
| `GET /api/auth/me` | Report current login state. |
| `GET /api/protected/message` | Protected by Spring Security. |

## Test each feature

1. Load the dashboard. The cards call status, items, and auth REST APIs.
2. Click **Call GET /api/hello** to see a Java/Spring Boot response.
3. Create a record, then reload, edit, and delete it. These actions go through REST → JPA/Hibernate → PostgreSQL.
4. Watch the Spring Boot terminal while doing CRUD to see Hibernate SQL.
5. Log in as `demo` with password `demo123`.
6. Call the protected endpoint; it now works because Spring Security recognizes the session.
7. Log out to remove that access.

## Git and GitHub

From the project root:

```bash
git init
git add .
git commit -m "Create full-stack test drive"
```

Create an empty GitHub repository, add it as `origin`, and push. The included `.gitignore` avoids committing dependencies, build files, and local environment settings.

## Deploy to Render

This repository is ready to deploy as one Render Blueprint. The frontend source remains in `frontend/` and the backend source remains in `backend/`, but the production Docker build exports the Next.js page and packages it into Spring Boot's static resources. The browser and REST API therefore share one public URL in production; no production frontend URL or CORS setting is required.

1. Push the complete `FULL-STACK-TEST` folder to a GitHub repository, with `render.yaml` at its root.
2. In Render, choose **New** → **Blueprint**, connect the GitHub repository, and select it.
3. Render reads `render.yaml`, creates both `full-stack-test-drive` and the PostgreSQL 16 database, wires the database's private connection string into `DATABASE_URL`, then deploys.
4. Open the URL Render gives to `full-stack-test-drive` and sign in with `demo` / `demo123`.

No Render dashboard environment variables, build commands, start commands, database credentials, CORS origins, or frontend API URLs need to be entered manually. The Blueprint selects Render's free web/database plans; choose a paid plan in Render only if you need the availability or capacity it provides.

For future commits, Render can redeploy from GitHub automatically. Render uses `GET /api/status` as its health check; that endpoint also confirms the database connection.

## Intentional simplifications

- CRUD endpoints are public, so their JPA/Hibernate flow stays easy to inspect. The dedicated protected endpoint demonstrates security.
- The `demo` user is in memory, not PostgreSQL.
- A real app should store users safely, use HTTPS, validate more broadly, and enable/handle CSRF.

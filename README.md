# Resume Improvement Agent

AI-powered resume analyzer that scores a resume against a job description, lists
matched/missing keywords, performs skill-gap analysis, and generates an improved
resume.

**Stack**

- **Frontend** — React + Vite
- **Backend** — Spring Boot (Java 25)
- **Database** — PostgreSQL 16

---

## Run with Docker (recommended)

Everything (database, backend, frontend) starts with a single command from the
project root.

| Action | Command |
| ------ | ------- |
| Start full project | `docker compose up --build` |
| Start in detached mode | `docker compose up -d --build` |
| Stop full project | `docker compose down` |
| Stop and delete database volume | `docker compose down -v` |
| Check running containers | `docker compose ps` |
| View backend logs | `docker compose logs -f backend` |
| View frontend logs | `docker compose logs -f frontend` |
| View database logs | `docker compose logs -f postgres` |

Once started:

| Service | URL |
| ------- | --- |
| Frontend | http://localhost:5174/ |
| Backend API | http://localhost:8081/ |
| PostgreSQL | `localhost:5556` (user `postgres`, password `postgres`, db `resume_agent_db`) |

> All commands work in Windows PowerShell. Requires Docker Desktop.

This project uses **unique host ports** (`5174`, `8081`, `5556`), a unique
Docker network (`resume-improvement-agent-net`) and named volume
(`resume-improvement-agent-pg-data`), so it can run alongside other Docker
projects without conflicts. Internal container ports remain standard
(`5173`, `8080`, `5432`).

Containers use `restart: unless-stopped`, so they come back automatically when
Docker Desktop restarts (until you explicitly `docker compose down`).

### Verify each service

1. **Database**

   ```powershell
   docker compose ps
   ```

   The `resume-pg` container should show status `healthy`. You can also connect:

   ```powershell
   docker exec -it resume-pg psql -U postgres -d resume_agent_db -c "\dt"
   ```

2. **Backend**

   ```powershell
   docker compose logs -f backend
   ```

   Wait for `Tomcat started on port 8080` (inside the container). Then test the
   history endpoint via the host port:

   ```powershell
   curl http://localhost:8081/api/resume/history
   ```

   It should return JSON (an empty array `[]` on a fresh database).

3. **Frontend**

   Open http://localhost:5174/ in the browser. Paste a resume + job
   description and click **Analyze Resume**. A successful response confirms the
   frontend → backend → database chain is working end-to-end.

---

## Local development (without Docker)

The original manual workflow still works.

**Prerequisite:** a PostgreSQL instance reachable on `localhost:5556`. The
simplest option is to start just the database from compose:

```powershell
docker compose up -d postgres
```

**Backend**

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

By default the backend connects to `jdbc:postgresql://localhost:5556/resume_agent_db`
and listens on port `8080`.

**Frontend**

```powershell
cd frontend
npm install
npm run dev
```

The frontend reads `VITE_API_BASE_URL` (fallback `http://localhost:8081`). If you
run the backend locally on port `8080`, set the variable before starting Vite:

```powershell
$env:VITE_API_BASE_URL = "http://localhost:8080"; npm run dev
```

---

## Configuration

### Backend environment variables

`backend/src/main/resources/application.properties` reads these values with safe local defaults, so the same build can run locally and in Docker.

| Variable                        | Default Local Value                                | Docker Value                                      |
| ------------------------------- | -------------------------------------------------- | ------------------------------------------------- |
| `SPRING_DATASOURCE_URL`         | `jdbc:postgresql://localhost:5556/resume_agent_db` | `jdbc:postgresql://postgres:5432/resume_agent_db` |
| `SPRING_DATASOURCE_USERNAME`    | `postgres`                                         | `postgres`                                        |
| `SPRING_DATASOURCE_PASSWORD`    | `<your-local-db-password>`                         | `<docker-db-password>`                            |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update`                                           | `update`                                          |

> Do not commit real database passwords, API keys, tokens, or production secrets. Use environment variables or a local `.env` file for sensitive values.

### Frontend environment variable

| Variable            | Default                 | Purpose                                               |
| ------------------- | ----------------------- | ----------------------------------------------------- |
| `VITE_API_BASE_URL` | `http://localhost:8081` | Base URL used by the frontend to call the backend API |

The frontend fallback configuration lives in `frontend/src/config.js`.

---

## Project structure

```
resume-improvement-agent/
├── docker-compose.yml        # Orchestrates all three services
├── README.md
├── backend/                  # Spring Boot app
│   ├── Dockerfile
│   ├── .dockerignore
│   └── src/...
└── frontend/                 # React + Vite app
    ├── Dockerfile
    ├── .dockerignore
    └── src/...
```

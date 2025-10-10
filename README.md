## Repository - Quick overview & navigation

This repository contains a small full-stack example showing how a Svelte frontend, a Spring Boot backend, and supporting services (H2, Redis, RabbitMQ) can work together. It's organized so each part can be developed and run independently or together using Docker Compose.

Top-level layout (where to find things):

- `backend` / Java sources — The repository places the Spring Boot source tree under `src/main/java/no/hvl/Lab/` (look for `LabApplication.java`, controllers, services and WebSocket code there). Application resources are under `src/main/resources/` (application.properties, static files, META-INF). The root Gradle files (`build.gradle.kts`, `settings.gradle.kts`, `gradlew`) live at the repository root and build the project from this `src` layout.
- `frontend/` — Svelte + Vite single page app. UI components and static assets are here (`src/`, `public/`).
- `docker-compose.yml` — Compose file that brings up the backend, Redis and RabbitMQ for a local integration environment.
- `Dockerfile` — Multi-stage Dockerfile used to build the backend image.
- `build.gradle.kts`, `settings.gradle.kts`, `gradlew` — Gradle build for the backend.

Quick navigation / how to run:

- Local development (backend):
   - From the repository root run: `./gradlew bootRun` to start the Spring Boot server.
- Local development (frontend):
   - Change to `frontend/` and run `npm install` then `npm run dev` to start the Vite dev server.
- With Docker Compose (recommended for consistent setup):
   - Build and start everything: `docker compose up --build`
   - App: http://localhost:8080
   - RabbitMQ UI: http://localhost:15672 (user `guest` / pass `guest`)

See the full project explanation below for details on architecture, components and usage.


## Project – Simple Explanation (In English)

The goal is to show how a small app can be built with several parts working together: backend (Spring Boot), frontend (Svelte), databases (H2 and Redis), messaging (RabbitMQ), and running in Docker.

---
## 1. Backend (Spring Boot)
Spring Boot is the “engine” on the server side.
- Receives HTTP calls (for example, /api/polls).
- Talks to the database to store and read data about users, polls, options, and votes.
- Sends messages when something happens (new poll, new vote) via RabbitMQ.
- Has WebSocket support so browsers can get “live” updates without refreshing.

Why: Spring Boot makes it quick to build an API without much setup.

---
## 2. Frontend (Svelte + Vite)
Svelte is what the user sees and clicks.
- Shows a list of polls.
- Lets you vote and create new polls.
- Can connect to WebSocket to get instant updates when others vote.

Why: Svelte is easy to learn and produces small, fast code in the browser.

---
## 3. H2 Database (in‑memory SQL)
H2 is a small database that runs in memory during development.
- Stores users, polls, options, and votes.
- Disappears when the app stops.

Why: Super fast to start with. No installation needed.

---
## 4. Redis (Cache / Fast storage)
Redis can be used as a “short‑term store” for data we look up often.
- Can cache things like vote counts so we don’t run heavy SQL queries every time.
- In this project it’s included as support if we want to optimize.
- Redis is used as a simple cache for vote counts: the first lookup reads from the database and stores it in Redis; when votes change, the cache is cleared so it’s rebuilt next time.

Why: Gives speed when many users ask for the same data.

---
## 5. RabbitMQ (Message queue)
RabbitMQ is a system for sending messages between parts of the solution.
- The backend publishes a message when someone votes or creates a poll.
- Listeners receive the message and can do things: store, forward, or update WebSocket.
- Messages use a “routing key” like `poll.3` (poll with id 3).

Why: Loosely couples things. Other systems can hook in later without changing the core.

Simple flow:
1. User votes in the frontend.
2. Frontend calls the backend API.
3. Backend saves the vote in the DB and publishes a Vote message to RabbitMQ.
4. A listener in the backend receives the message and can send a WebSocket update.
5. Other services (if we add them later) can also listen to the same message.

---
## 6. Docker
Docker packages things so they run the same everywhere.
- RabbitMQ often runs in a Docker container.
- We can also package backend and frontend in containers later.

Why: Less “it works on my machine but not on yours”.

---
## 7. WebSocket (Live updates)
Instead of the frontend asking all the time, the backend can push a message: “Someone voted!”
- When a vote comes in and we receive the message, a small JSON object is sent to all connected users.
- The frontend updates the numbers without a page refresh.

---
## 8. Logic – How things connect

Short description of the main flow:
- User creates a poll -> backend saves in DB -> sends PollCreated message -> listener receives -> (can prepare things / send info)
- User votes -> backend saves in DB -> sends Vote message -> listener receives -> sends WebSocket update -> frontend shows new vote count

Key idea: We separate “an event happened” (publish message) from “what we do with it” (listener). This makes it easy to add new reactions later (for example, a statistics service) without changing core functions.

---
## 9. Why this architecture?
- Easy to swap one part (for example, replace H2 with Postgres) without removing the rest.
- Messages via RabbitMQ make the system more flexible and scalable.
- Frontend and backend can be developed separately.
- WebSocket gives a live feel.
- Cache (Redis) can add speed when traffic grows.

---
## 10. How to run (simple)
You can run the app in two ways: local (normal) or with Docker/Compose.

### A) Local
1. Start RabbitMQ (for example with Docker image `rabbitmq:3.13-management`).
2. Run the backend:

```sh
./gradlew bootRun
```

3. Run the frontend (inside `Frontend/`):

```sh
npm install
npm run dev
```

4. Open your browser (for example, http://localhost:5173).

### B) With Docker/Compose (recommended for consistent setup)
1. Build and start everything:

```sh
docker compose up --build
```

2. Visit:
   - App: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:polls`, user `sa`)
   - RabbitMQ UI: http://localhost:15672 (user `guest`, password `guest`)

3. Stop everything:

```sh
docker compose down
```

Test messages without CLI:
- Publish a vote: POST to /admin/publish/vote
- See queue status: GET /admin/queues/poll-events

---
## 11. Future improvements (ideas)
- Add a real database (Postgres/MySQL).
- Add security (login, roles).
- A separate service for vote analytics.

---
## 12. Glossary (simple words)
- API: Door into the backend (URL + data).
- Queue: List of messages waiting.
- Message broker: Post office for messages.
- Event: “Something happened” message.
- Cache: Fast temporary storage.
- Container: Package that runs the same everywhere.

---
## 13. Quick summary
Frontend (Svelte) talks to Backend (Spring Boot). Backend stores in H2 and sends messages to RabbitMQ. Listeners receive messages and update users via WebSocket. Redis can help with speed. Docker makes it easy to run everything the same way. Everything is split so we can grow later.

Good luck—ask if you want more details on any part!

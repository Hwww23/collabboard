# CollabBoard

![CI](https://github.com/Hwww23/collabboard/actions/workflows/ci.yml/badge.svg)

A real-time collaborative task board. Think Trello with live WebSocket updates.

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot, Spring Security |
| Real-time | WebSockets (STOMP) |
| Database | PostgreSQL + JPA |
| Frontend | Next.js, TypeScript, Tailwind |
| Auth | JWT |
| Deployment | Docker, Docker Compose |

## Architecture

```
Next.js Frontend (port 3000)
        │
        │ REST API + WebSocket
        ▼
Spring Boot Backend (port 8080)
        │
        ├── JWT auth (Spring Security)
        ├── REST endpoints (/api/boards, /api/auth)
        └── WebSocket broker (/ws)
                │ STOMP /topic/board/{id}
                └── broadcasts to all connected clients
        │
        ▼
PostgreSQL (JPA / Hibernate)
```

## Running with Docker

```bash
docker-compose up --build
```

Open `http://localhost:3000`

## Features

- Register and login with JWT auth
- Create boards with default columns (To Do, In Progress, Done)
- Invite others via invite code
- Create and drag tasks between columns
- Real-time updates — all users on the same board see changes instantly via WebSocket
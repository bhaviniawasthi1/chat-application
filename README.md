# SyncTalk

A real-time, two-seat chat demo. There are exactly two accounts, ever — grab
one, send the link to a friend, and have them grab the other. Log out and
your seat opens up for the next pair of visitors.

## Live demo

Two accounts, `alex` / `sam` — credentials are shown right on the landing
page. Pick one, share the link, and chat with whoever grabs the other seat.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot (Web, Security, WebSocket, Data JPA) |
| Real-time transport | WebSocket via STOMP/SockJS |
| Database | H2, in-memory, reset on every restart |
| Frontend | Thymeleaf + plain HTML/CSS/JS |
| Build | Maven |
| Deploy | Docker / Render |

## Why H2 and why it resets

This is a portfolio project, not a product — there's no real user data to
protect, so keeping a persistent database around would just be overhead.
`spring.jpa.hibernate.ddl-auto=create` rebuilds the schema from scratch on
every boot, and a `DataInitializer` reseeds the two demo accounts and wipes
chat history at the same time. Restarting the server (including Render's
free-tier sleep/wake cycle) gives everyone a clean slate.

## The "in use" seat lock

Only two accounts exist, so only two people can ever be in the app at once.
The moment someone logs into `alex` or `sam`, that account is marked in use
and a third login attempt on it is rejected (via Spring Security's account
"locked" check) until the current occupant logs out, which frees the seat
immediately.

## Local setup

Requires Java 17+ and Maven 3.6+. No external database to install — H2 runs
in-memory.

```bash
mvn spring-boot:run
```

Open `http://localhost:8080` in two separate browser windows (or one normal
+ one incognito) to log in as both `alex` and `sam` and chat with yourself.

## Project structure

```
SyncTalk/
├── Dockerfile
├── render.yaml
├── pom.xml
└── src/main/
    ├── java/com/synctalk/
    │   ├── SyncTalkApplication.java
    │   ├── config/          # WebSocket config, demo account seeding
    │   ├── controller/      # Page routes + chat WebSocket handling
    │   ├── model/           # User, ChatMessage JPA entities
    │   ├── repository/      # Spring Data repositories
    │   └── security/        # Spring Security config + seat locking
    └── resources/
        ├── templates/       # landing.html, login.html, chat.html
        ├── static/css/
        └── application.properties
```

## Deploying on Render

1. Push this repo to GitHub.
2. On Render, create a new **Web Service** from the repo, runtime **Docker**
   (Render will pick up `render.yaml` and `Dockerfile` automatically).
3. No database service needed — H2 is in-memory.
4. Deploy. Every restart gives a fresh set of demo accounts and empty chat
   history.

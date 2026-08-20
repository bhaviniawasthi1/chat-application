# SyncTalk

A real-time chat app built to demo WebSocket messaging, live — typing
indicators, online presence, and reply-to-message included. No sign-up:
pick a persona (John or Emily) on the landing page and you're straight
into the conversation.

## Live demo

**[sync-talk.onrender.com](https://sync-talk.onrender.com/)**

Two personas, `john` / `emily` — one click on the landing page logs you
straight in with credentials pre-filled. Open a second tab (or send the
link to a friend) to grab the other one and chat in real time. Render's
free tier spins the service down when idle, so the first load after a
quiet stretch can take a few extra seconds to wake up.

## Features

- **Real-time messaging** over WebSocket (STOMP/SockJS) — no polling.
- **Typing indicator** — the other persona's status line shows "is
  typing…" while you're mid-message, and clears itself a couple
  seconds after you stop.
- **Online presence** — the chat header reflects whether the other
  persona currently has the app open, live.
- **Reply to a message** — click any bubble to quote it; the quoted
  snippet rides along with your reply, WhatsApp-style.
- **Seat locking** — exactly two personas exist, and only one person
  can hold each at a time (see below).
- **WhatsApp-style header** — the other person's avatar, name, and
  live status sit top-left; your own avatar and logout sit top-right.

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
every boot, and a `DataInitializer` reseeds the two demo personas and wipes
chat history at the same time. Restarting the server (including Render's
free-tier sleep/wake cycle) gives everyone a clean slate.

## The seat lock

Two personas exist so two people can be in the app at once. The moment
someone logs into `john` or `emily`, that persona is marked in use and a
third login attempt on it is rejected (via Spring Security's account
"locked" check) until the current occupant logs out, which frees the seat
immediately.

## Local setup

Requires Java 17+ and Maven 3.6+. No external database to install — H2 runs
in-memory.

```bash
mvn spring-boot:run
```

Open `http://localhost:8080` in two separate browser windows (or one normal
+ one incognito) to log in as both `john` and `emily` and chat with yourself.

## Project structure

```
SyncTalk/
├── Dockerfile
├── render.yaml
├── pom.xml
└── src/main/
    ├── java/com/synctalk/
    │   ├── SyncTalkApplication.java
    │   ├── config/           # WebSocket config, demo account seeding,
    │   │                     # online-presence tracking
    │   ├── controller/       # Page routes + chat/typing/presence
    │   │                     # WebSocket handling
    │   ├── model/            # User, ChatMessage JPA entities
    │   ├── repository/       # Spring Data repositories
    │   └── security/         # Spring Security config + seat locking
    └── resources/
        ├── templates/        # landing.html, login.html, chat.html
        ├── static/
        │   ├── css/
        │   ├── img/          # persona avatars
        │   ├── favicon.ico
        │   └── favicon.png
        └── application.properties
```

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">
  <p>Built by <strong>Bhavini Awasthi</strong></p>
  <p>
    <a href="https://www.linkedin.com/in/bhaviniawasthi/">LinkedIn</a>
  </p>
</div>


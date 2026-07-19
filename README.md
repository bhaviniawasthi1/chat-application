# SyncTalk

A real-time chat application with user authentication, private messaging, and group chat features.

## Features

- **User Authentication** — Register and login with username/password
- **Private Chat** — Real-time one-on-one messaging with message history
- **Group Chat** — Create groups, add members, and have group conversations
- **Message History** — All messages stored and loaded from MySQL database
- **Real-time Updates** — Instant message delivery via Java Socket threads

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | JavaFX (FXML, CSS) |
| Backend | Java Sockets (ServerSocket) |
| Database | MySQL |
| Build | Maven |
| Serialization | JSON (Gson) |

## Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.0+

## Setup

### 1. Database

Create the database and tables:

```sql
CREATE DATABASE IF NOT EXISTS synctalk;
USE synctalk;
-- then run database/schema.sql
```

### 2. Configuration

Open `src/main/java/server/DatabaseManager.java` and update the MySQL credentials:

```java
private static final String USER = "root";     // your MySQL username
private static final String PASSWORD = "root"; // your MySQL password
```

### 3. Run the Server

```bash
mvn clean compile exec:java
```

The server starts on port `8080`.

### 4. Run the Client

Open a separate terminal:

```bash
mvn javafx:run
```

Launch multiple client instances to test messaging between users.

## Usage

1. **Register** — Create a new account with username, password, and display name
2. **Login** — Sign in with your credentials
3. **Private Chat** — Double-click any user in the online users list
4. **Group Chat** — Click "+ New Group", select members, then double-click the group
5. **Refresh** — Click the Refresh button to reload users and groups

## Project Structure

```
SyncTalk/
├── database/schema.sql
├── pom.xml
├── src/main/java/
│   ├── server/
│   │   ├── SyncTalkServer.java       # Entry point, accepts client connections
│   │   ├── ClientHandler.java        # Per-client thread, handles JSON messages
│   │   └── DatabaseManager.java      # MySQL connection and queries
│   └── client/
│       ├── SyncTalkClient.java       # JavaFX application entry, page navigation
│       ├── ServerConnection.java     # Socket connection with event dispatch
│       ├── LoginController.java
│       ├── RegisterController.java
│       ├── MainController.java       # Dashboard with user/group lists
│       ├── ChatController.java       # Private chat view
│       ├── GroupChatController.java  # Group chat view
│       └── CreateGroupController.java
└── src/main/resources/
    ├── css/style.css
    └── fxml/*.fxml
```

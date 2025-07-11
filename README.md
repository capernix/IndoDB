# 🎮 IndiaDB Games - Game Discovery & Deal Intelligence Platform

A full-stack platform for Indian gamers to discover the best deals, track pricing history, and engage with community insights across Steam, Epic Games, GOG, and more.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven

### Setup

1. **Clone the repository:**
   ```bash
   git clone <your-repo-url>
   cd IndiaDB
   ```

2. **Set up environment:**
   ```bash
   cp .env.example .env
   # Update .env with your API keys when ready
   ```

3. **Start infrastructure services:**
   ```bash
   docker-compose up -d postgres redis kafka zookeeper
   ```

4. **Run the Spring Boot application:**
   ```bash
   cd games-backend
   ./mvnw spring-boot:run
   ```

5. **Verify setup:**
   - Database: http://localhost:5432 (PostgreSQL)
   - Redis: http://localhost:6379
   - Kafka UI: http://localhost:8080
   - API: http://localhost:8081 (when app is running)

## 🏗️ Project Structure

```
IndiaDB/
├── games-backend/          # Spring Boot application
│   ├── src/main/java/com/indodb/games_backend/
│   │   ├── model/         # JPA entities
│   │   ├── repository/    # Data access layer (coming soon)
│   │   ├── service/       # Business logic (coming soon)
│   │   └── controller/    # REST controllers (coming soon)
├── database/
│   └── init/             # Database initialization scripts
├── docker-compose.yml    # Local development environment
└── docs/                 # Project documentation
```

## 🎯 Current Status: Phase 1 (Foundation)

- ✅ Database schema and sample data
- ✅ Docker infrastructure setup
- ✅ Core domain models (Game, Platform, GamePrice)
- 🚧 Repository layer (in progress)
- 🚧 Service layer (planned)
- 🚧 REST API endpoints (planned)

## 🔧 Tech Stack

- **Backend:** Spring Boot 3.x, Java 17
- **Database:** PostgreSQL 15, Redis 7
- **Messaging:** Apache Kafka
- **Build:** Maven
- **Containerization:** Docker, Docker Compose

## 📋 Development Phases

1. **Phase 1 (Weeks 1-3):** Foundation & Steam API integration
2. **Phase 2 (Weeks 4-6):** Multi-platform integration (Epic, GOG)
3. **Phase 3 (Weeks 7-9):** User features & authentication
4. **Phase 4 (Weeks 10-12):** Community features & analytics

## 🤝 Contributing

This is a learning project. Feel free to suggest improvements or report issues.

## 📄 License

This project is for educational purposes.

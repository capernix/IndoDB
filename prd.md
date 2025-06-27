🎮 Project Title: IndiaDB Games – Full-Stack Game Discovery & Deal Intelligence Platform
📄 Description:
IndiaDB Games is a production-grade, full-stack platform designed to help Indian gamers discover the best deals, track pricing history, and engage with community-curated insights across Steam, Epic Games, GOG, and more. Inspired by platforms like SteamDB, it extends functionality with real-time analytics, user-generated content, wishlist price alerts, and weekly free game highlights — tailored specifically for the Indian market.

The platform consists of modular, containerized microservices, including a Spring Boot backend API, Kafka-based data ingestion pipeline, PySpark-powered analytics, and a responsive React/Next.js frontend. Game metadata, prices, user votes, and forum discussions are stored and processed using PostgreSQL, Redis, and Dockerized workers, while WebSocket-based real-time features support interactive leaderboards and alert notifications.

🔧 Core Features
🧠 Smart Game Data Aggregator
Integrates with official APIs from Steam, Epic Games, and GOG to fetch game metadata legally and efficiently.

**Steam Web API Integration:**
- Uses Steam Web API (api.steampowered.com) with proper API keys
- Fetches game details, pricing, reviews, news, and user stats
- Available endpoints: ISteamNews, ISteamUserStats, ISteamApps, ISteamStore
- Supports JSON/XML output formats with proper rate limiting

**Epic Games Store API:**
- Integrates with Epic Games Developer Portal APIs
- Fetches weekly free games data and store information
- Proper OAuth authentication for user-specific data

**GOG Galaxy API:**
- Uses GOG's developer APIs for game catalog data
- Integrates with GOG Galaxy SDK for enhanced features
- Accesses DRM-free game information and pricing

Kafka topic: game.price.update, consumed and stored in the main PostgreSQL DB.

📊 Analytics Engine
Uses PySpark batch jobs to calculate:

Most discounted genres monthly

Publisher-wise pricing patterns

Price prediction trends using historical data

Dashboards visualized with Recharts or D3.js

📋 User Account System
Users can register/login via JWT-based auth.

Manage wishlist of games across platforms.

Receive email/Telegram/Discord alerts when a wished game’s price drops below a threshold.

Rate and vote for their favorite game of the month.

🗳️ Popularity Voting & Leaderboards
Monthly voting system with per-user deduplication.

Leaderboard of top-voted games, refreshes in real time via WebSockets + Redis.

💬 Game Forums & Mini Reviews
Each game has a community discussion board.

Users can post tips, questions, or reviews.

Supports nested replies, likes, and moderation tools for admins.

🎁 Epic Games Weekly Free Game Tracker
Dedicated component to detect and display weekly free games with timers and alert buttons.

Also visible on homepage carousel or “Free Games” tab.

🧱 Tech Stack Breakdown
Area	Technology
Frontend	React or Next.js + Tailwind CSS, Recharts (for graphs), WebSockets
Backend API	Spring Boot (JWT auth, REST, validation, service-layer architecture)
Database	PostgreSQL (main DB), Redis (caching votes, hot games, and alerts)
Data Ingestion	Kafka + Python/Go-based API integration bots (scheduled via cron or Airflow)
Analytics Engine	PySpark batch jobs on price history & user activity logs
Monitoring	Prometheus + Grafana (for service health and scraper uptime)
CI/CD & Infra	Docker, docker-compose, NGINX reverse proxy, GitHub Actions
Optional Add-ons	Steam OAuth, Epic Games OAuth, GOG Galaxy integration, Telegram Bot API, Admin Panel for API management/dashboard control

## 🔌 API Integrations & Data Sources

### **Steam Web API**
- **Endpoint:** `https://api.steampowered.com/`
- **Authentication:** Web API Key (free registration required)
- **Key Services:**
  - `ISteamApps/GetAppList` - Get all Steam app IDs and names
  - `ISteamNews/GetNewsForApp` - Game news and updates
  - `ISteamUserStats/GetGlobalAchievementPercentagesForApp` - Game statistics
  - `ISteamStore/GetAppDetails` - Detailed game information including pricing
- **Rate Limits:** 100,000 calls per day per key
- **Indian Price Support:** ✅ Steam supports INR pricing

### **Epic Games Store API**
- **Integration:** Epic Games Developer Portal APIs
- **Features:**
  - Weekly free games detection
  - Store catalog access
  - User library integration (with OAuth)
- **Rate Limits:** TBD based on developer agreement
- **Indian Price Support:** ✅ Epic supports regional pricing

### **GOG Galaxy API**
- **SDK Integration:** GOG Galaxy SDK for comprehensive features
- **Developer Portal:** Access to catalog and user data
- **Features:**
  - DRM-free game catalog
  - User authentication via GOG accounts
  - Achievement and statistics tracking
- **Indian Price Support:** ✅ GOG supports regional currencies

### **Compliance & Best Practices**
- ✅ **Terms of Service Compliance:** All integrations follow platform ToS
- ✅ **Rate Limiting:** Implemented exponential backoff and request throttling
- ✅ **API Key Security:** Stored in environment variables, never in code
- ✅ **Data Attribution:** Proper crediting of data sources as required
- ✅ **Caching Strategy:** Redis caching to minimize API calls
- ✅ **Error Handling:** Graceful degradation when APIs are unavailable

## 📋 Implementation Phases & API Strategy

### **Phase 1: Foundation (Weeks 1-3)**
- Set up Steam Web API integration with basic game data fetching
- Implement core database schema for games, prices, and platforms
- Create basic Kafka producer for Steam data ingestion
- Build simple REST API endpoints for game search

### **Phase 2: Multi-Platform Integration (Weeks 4-6)**
- Add Epic Games Store API integration
- Implement GOG Galaxy API integration
- Build unified data model for cross-platform game information
- Create price comparison and tracking features

### **Phase 3: User Features (Weeks 7-9)**
- User authentication system with JWT
- Wishlist functionality with price alerts
- Email/notification system for price drops
- Basic frontend for game browsing and wishlists

### **Phase 4: Advanced Features (Weeks 10-12)**
- Community features (voting, forums, reviews)
- Real-time WebSocket notifications
- Analytics dashboard with PySpark
- Free game tracking and alerts

### **API Rate Limit Management Strategy**
```python
# Example rate limiting implementation
class APIRateLimiter:
    def __init__(self, calls_per_minute=60):
        self.calls_per_minute = calls_per_minute
        self.calls = []
    
    def can_make_request(self):
        now = time.time()
        # Remove calls older than 1 minute
        self.calls = [call_time for call_time in self.calls if now - call_time < 60]
        return len(self.calls) < self.calls_per_minute
```

✨ Highlights & Resume Impact
Designed a modular, scalable architecture using microservices and event-driven pipelines.

Demonstrated expertise in Kafka messaging, real-time web features, and complex backend logic.

Implemented a user-centric feature suite including wishlist tracking, forum discussions, and alert systems.

Focused on a real-world problem space, applying engineering skills in a meaningful and high-utility domain.

Would you like me to:

Generate a GitHub repository scaffold for this project?

Create a Notion roadmap with weekly milestones?

Suggest actual sources/APIs to scrape or pull from (Steam, Epic, etc.)?

Let me know how you'd like to proceed!
# Docker Compose for IndiaDB Games Development Environment

## Quick Start

1. **Copy environment file:**
   ```bash
   cp .env.example .env
   ```

2. **Update API keys in `.env` file:**
   - Get Steam API key from: https://steamcommunity.com/dev/apikey
   - Get Epic Games API credentials from Epic Games Developer Portal
   - Get GOG API credentials from GOG Developer Portal

3. **Start infrastructure services:**
   ```bash
   docker-compose up -d postgres redis kafka zookeeper
   ```

4. **Wait for services to be healthy (check with):**
   ```bash
   docker-compose ps
   ```

5. **View Kafka topics (optional):**
   ```bash
   docker-compose up -d kafka-ui
   # Access at http://localhost:8080
   ```

## Services Included

### Core Infrastructure
- **PostgreSQL 15** (Port 5432) - Main database
- **Redis 7** (Port 6379) - Cache and session store
- **Apache Kafka** (Port 9092) - Event streaming
- **Zookeeper** (Port 2181) - Kafka coordination

### Development Tools
- **Kafka UI** (Port 8080) - Kafka topic management
- **Spring Boot App** (Port 8081) - Your application (when ready)

## Useful Commands

### Start all services:
```bash
docker-compose up -d
```

### Start only infrastructure (without app):
```bash
docker-compose up -d postgres redis kafka zookeeper
```

### View logs:
```bash
docker-compose logs -f [service-name]
```

### Stop all services:
```bash
docker-compose down
```

### Stop and remove volumes (⚠️ Will delete data):
```bash
docker-compose down -v
```

### Connect to PostgreSQL:
```bash
docker-compose exec postgres psql -U indiadb_user -d indiadb_games
```

### Connect to Redis CLI:
```bash
docker-compose exec redis redis-cli
```

### Check service health:
```bash
docker-compose ps
```

## Database Access

- **Host:** localhost
- **Port:** 5432
- **Database:** indiadb_games
- **Username:** indiadb_user
- **Password:** indiadb_password

## Redis Access

- **Host:** localhost
- **Port:** 6379
- **No password** (development only)

## Kafka Access

- **Bootstrap Servers:** localhost:9092
- **Kafka UI:** http://localhost:8080

## Volumes

The following data is persisted:
- `postgres_data` - PostgreSQL database files
- `redis_data` - Redis data files
- `kafka_data` - Kafka topic data

## Network

All services run on the `indiadb-network` bridge network, allowing them to communicate using service names as hostnames.

## Next Steps

1. Configure your Spring Boot `application.yml` to connect to these services
2. Uncomment the `app` service in `docker-compose.yml` when ready to containerize your Spring Boot app
3. Create a `Dockerfile` in your backend directory

# Multiplayer Game Matchmaking Engine

A scalable matchmaking engine that pairs players in real-time based on skill level and network latency.

## Features

- **Skill-based Matching**: Elo/MMR-style scoring system
- **Latency-aware Pairing**: Ensures low-latency matches
- **Redis-backed Queue**: High-performance concurrent matchmaking
- **REST API**: Easy integration with game clients
- **Docker Support**: Containerized deployment
- **Horizontal Scaling**: Support for multiple matchmaking nodes

## Tech Stack

- Java 17
- Spring Boot 3.2
- Redis (Lettuce client)
- Docker & Docker Compose
- Maven

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose (for containerized deployment)

### Running with Docker Compose

\`\`\`bash
# Build and start all services
docker-compose up --build

# The API will be available at http://localhost:8080
\`\`\`

### Running Locally

\`\`\`bash
# Start Redis
docker run -d -p 6379:6379 redis:7-alpine

# Build the project
mvn clean package

# Run the application
java -jar target/matchmaking-engine-1.0.0.jar
\`\`\`

## API Endpoints

### Join Queue
\`\`\`bash
POST /api/matchmaking/joinQueue
Content-Type: application/json
Authorization: Bearer <token>

{
  "playerId": "player123",
  "username": "ProGamer",
  "skillRating": 1500,
  "latency": 45,
  "region": "us-east"
}
\`\`\`

### Leave Queue
\`\`\`bash
POST /api/matchmaking/leaveQueue
Content-Type: application/json
Authorization: Bearer <token>

{
  "playerId": "player123"
}
\`\`\`

### Get Match Result
\`\`\`bash
GET /api/matchmaking/matchResult/{playerId}
Authorization: Bearer <token>
\`\`\`

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Matchmaking Configuration
matchmaking.skill.max-gap=200          # Maximum skill rating difference
matchmaking.latency.max-threshold=100  # Maximum latency in ms
matchmaking.queue.timeout=30000        # Queue timeout in ms
matchmaking.match.size=2               # Players per match

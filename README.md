# social-media-api

A Spring Boot microservice that acts as an API gateway and guardrail system for a social media platform. It uses Redis for real-time concurrency control, virality scoring, and smart notification batching — with PostgreSQL as the persistent source of truth.

---

## Tech Stack

- **Java 17** / Spring Boot 3.x
- **PostgreSQL 15** — persistent storage
- **Redis 7** — atomic counters, TTL-based locks, notification queues
- **Docker Compose** — local infrastructure setup

---

## Getting Started

**1. Start PostgreSQL and Redis**
```bash
docker-compose up -d
```

**2. Run the application**
```bash
./mvnw spring-boot:run
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/posts` | Create a new post |
| POST | `/api/posts/{postId}/comment` | Add a comment (bot or human) |
| POST | `/api/posts/{postId}/like` | Like a post |

All responses follow a standard envelope:
```json
{
  "success": true,
  "message": "Post created successfully",
  "data": { },
  "timestamp": "2025-01-01T12:00:00"
}
```

### POST `/api/posts`
```json
{ "authorId": 1, "content": "Hello World" }
```

### POST `/api/posts/{postId}/comment`
```json
{ "authorId": 2, "content": "Nice post!", "botId": 5 }
```
If `botId` is present, all bot guardrails are enforced before saving.

### POST `/api/posts/{postId}/like`
```json
{ "userId": 3 }
```

---

## Redis Key Schema

| Key | Type | TTL | Purpose |
|-----|------|-----|---------|
| `post:{id}:virality_score` | String | — | Running virality score |
| `post:{id}:bot_count` | String | — | Total bot replies (cap: 100) |
| `cooldown:bot:{botId}:human:{humanId}` | String | 10 min | Per-bot cooldown per human |
| `notification_throttle:user:{id}` | String | 15 min | Notification rate limiter |
| `user:{id}:pending_notifications` | List | — | Batched notification queue |

---

## Virality Scoring

| Interaction | Score |
|-------------|-------|
| Bot reply | +1 |
| Human like | +20 |
| Human comment | +50 |

Scores are incremented in Redis instantly on each interaction via `INCR`.

---

## How Thread Safety Is Guaranteed

The core challenge is the **horizontal cap** — only 100 bot replies are allowed per post, even under hundreds of concurrent requests.

**The approach: Redis `INCR` atomicity**

Redis executes commands sequentially in a single thread. The `INCR` command atomically reads, increments, and returns the new value — no two concurrent requests can interleave on it.

```
INCR post:{id}:bot_count  →  returns new value N
  if N > 100 → reject with 429 (Too Many Requests)
  if N ≤ 100 → proceed to DB write
```

This means the 101st concurrent request will receive exactly `101` from Redis and be rejected — the counter never drifts, regardless of traffic volume.

**Why not Java locks (`synchronized`)?**

Java-level locks are scoped to a single JVM. In a multi-instance deployment, each replica has its own memory, so in-process locks provide no cross-service protection. All counters, cooldowns, and queues live exclusively in Redis, keeping the application fully stateless and horizontally scalable.

---

## Notification Engine

**Immediate path** — if no notification was sent in the last 15 minutes, log it to console and set the throttle key.

**Throttled path** — if the throttle key exists, push the message to `user:{id}:pending_notifications` in Redis.

**CRON sweeper** — a `@Scheduled` task runs every 5 minutes, collects all pending notifications per user, and logs a single summarized message:
```
Summarized Push Notification: Bot X and 4 others interacted with your posts.
```
The Redis list is cleared after each sweep.

---

## Project Structure

```
src/main/java/com/social/api/
├── config/         # RedisTemplate configuration
├── controller/     # REST layer
├── dto/            # Request / Response models
├── entity/         # JPA entities (User, Bot, Post, Comment, Like)
├── repository/     # Spring Data JPA repositories
├── service/        # Service interfaces
└── serviceImp/     # Business logic + Redis guard + Scheduler
```

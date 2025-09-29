# DAT250 Lab - Redis Cache Implementation

This project demonstrates the integration of Redis caching with a Spring Boot + JPA application for DAT250 Exercise Assignment 5.

## Features Implemented

### 1. Redis Cache Integration
- **Redis dependency**: Added `redis.clients:jedis:6.2.0` to the project
- **Cache Service**: `PollCacheService` implements cache-aside pattern using Redis Hash datatype
- **Configuration**: `RedisConfig` provides Redis connection management

### 2. Poll Vote Caching
The application implements caching for poll vote aggregation:

- **Cache Key Pattern**: `poll:votes:{pollId}`
- **Data Structure**: Redis Hash mapping option IDs to vote counts
- **TTL**: 5 minutes (300 seconds) for cached data

### 3. Cache Operations

#### Cache-Aside Pattern
```java
// 1. Check cache first
Map<Long, Integer> cachedCounts = cacheService.getCachedVoteCounts(pollId);
if (cachedCounts != null) {
    return cachedCounts; // Cache hit
}

// 2. Cache miss - query database
Map<Long, Integer> voteCounts = computeVoteCountsFromDatabase(pollId);

// 3. Store in cache for future requests
cacheService.cacheVoteCounts(pollId, voteCounts);
```

#### Cache Invalidation
- Automatically invalidates cache when votes are cast or changed
- Ensures data consistency between database and cache

## Installation & Setup

### 1. Install Redis
Choose one of these options:

```bash
# Option 1: Install locally (macOS)
brew install redis
brew services start redis

# Option 2: Run with Docker
docker run -d -p 6379:6379 --name redis redis:latest

# Option 3: Use cloud service (Redis Cloud, AWS ElastiCache, etc.)
```

### 2. Verify Redis Connection
```bash
redis-cli ping
# Should respond with PONG
```

### 3. Start the Application
```bash
./gradlew bootRun
```

## API Endpoints

### Get Cached Vote Counts
```http
GET /api/polls/{pollId}/vote-counts
```

Returns a map of option IDs to vote counts, utilizing the Redis cache.

**Example Response:**
```json
{
  "1": 15,
  "2": 8,
  "3": 23
}
```

### Standard Poll Operations
- `POST /api/polls` - Create poll (triggers cache setup)
- `GET /api/polls/{pollId}` - Get poll details
- `POST /api/polls/{pollId}/votes` - Cast vote (invalidates cache)

## Redis CLI Experiments

### Use Case 1: Logged-in Users (Set Operations)
```bash
# Add users to logged-in set
SADD logged_in_users alice
SADD logged_in_users bob

# Check who's logged in
SMEMBERS logged_in_users

# Remove user (logout)
SREM logged_in_users alice

# Check if user is logged in
SISMEMBER logged_in_users bob
```

### Use Case 2: Poll Data (Hash Operations)
```bash
# Store poll vote counts as hash
HSET poll:votes:1 option:1 269 option:2 268 option:3 42

# Get all vote counts for a poll
HGETALL poll:votes:1

# Increment vote count
HINCRBY poll:votes:1 option:1 1

# Get specific option count
HGET poll:votes:1 option:1

# Set expiration
EXPIRE poll:votes:1 300
```

## Architecture Benefits

### Performance Improvement
- **Database Query Reduction**: Vote count aggregation queries are expensive
- **Fast Response Times**: Redis responds in microseconds vs milliseconds for SQL
- **Scalability**: Reduces load on primary database

### Cache Strategy
- **Cache-Aside**: Application manages cache explicitly
- **TTL-based Expiration**: Automatic cleanup of stale data
- **Invalidation on Write**: Ensures consistency when data changes

## Database vs Cache Query Comparison

### Without Cache (Direct Database Query)
```sql
SELECT o.presentation_order, COUNT(v.id)
FROM vote_options o 
INNER JOIN votes v on o.id = v.voted_on 
WHERE o.poll = ?
GROUP BY o.presentation_order
ORDER BY o.presentation_order;
```

### With Cache (Redis Hash)
```bash
HGETALL poll:votes:{pollId}
```

## Testing

### Unit Tests
```bash
./gradlew test
```

The `RedisCacheTest` class demonstrates:
- Basic cache operations
- Cache hit/miss scenarios  
- Increment operations
- Cache invalidation

### Manual Testing
1. Start Redis server
2. Start the application
3. Create a poll via REST API
4. Cast some votes
5. Call the vote-counts endpoint multiple times
6. Check Redis CLI to see cached data

## Configuration

### Redis Connection
Default connection: `redis://localhost:6379`

To use a different Redis instance, modify `RedisConfig.java`:
```java
@Bean
public UnifiedJedis jedis() {
    return new UnifiedJedis("redis://your-redis-host:6379");
}
```

### Cache TTL
Modify `CACHE_TTL_SECONDS` in `PollCacheService.java` to change expiration time.

## Error Handling

The application gracefully handles Redis connection failures:
- Cache misses fall back to database queries
- Redis connection errors don't crash the application
- Cache operations are non-blocking

## Next Steps

For production deployment, consider:
- Redis connection pooling
- Cache warming strategies  
- Monitoring and metrics
- Redis clustering for high availability
- Cache versioning for schema changes
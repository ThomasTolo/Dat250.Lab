# How to Run Redis - Complete Guide

## 🚀 Quick Start (Choose One Option)

### Option 1: Install Redis Locally (Recommended for Development)

#### macOS (using Homebrew)
```bash
# Install Redis
brew install redis

# Start Redis as a service (runs in background)
brew services start redis

# OR start Redis manually (runs in foreground)
redis-server

# Test connection
redis-cli ping
# Should respond: PONG
```

#### Windows (using WSL or native)
```bash
# Using WSL (Windows Subsystem for Linux)
sudo apt update
sudo apt install redis-server

# Start Redis
sudo service redis-server start

# Test connection
redis-cli ping
```

#### Linux (Ubuntu/Debian)
```bash
# Install Redis
sudo apt update
sudo apt install redis-server

# Start Redis service
sudo systemctl start redis-server
sudo systemctl enable redis-server  # Auto-start on boot

# Test connection
redis-cli ping
```

### Option 2: Run Redis with Docker (Easiest)

```bash
# Pull and run Redis container
docker run -d -p 6379:6379 --name redis redis:latest

# Verify it's running
docker ps

# Connect to Redis CLI through Docker
docker exec -it redis redis-cli

# Test connection
ping
# Should respond: PONG

# Stop Redis when done
docker stop redis

# Remove container
docker rm redis
```

### Option 3: Use Redis Cloud (No Installation)

1. Go to [Redis Cloud](https://redis.com/redis-enterprise-cloud/)
2. Create free account
3. Create a database
4. Get connection details
5. Update your `RedisConfig.java` with the cloud connection string

## 🔧 Testing Your Redis Installation

### 1. Basic Connection Test
```bash
# Connect to Redis CLI
redis-cli

# Inside Redis CLI, test basic commands
127.0.0.1:6379> ping
PONG

127.0.0.1:6379> set test "Hello Redis"
OK

127.0.0.1:6379> get test
"Hello Redis"

127.0.0.1:6379> exit
```

### 2. Test the Assignment Use Cases

#### Use Case 1: Logged-in Users (Sets)
```bash
redis-cli

# Track logged-in users
SADD logged_in_users alice bob eve
SMEMBERS logged_in_users
SREM logged_in_users alice
SMEMBERS logged_in_users
```

#### Use Case 2: Poll Vote Counts (Hashes)
```bash
# Store poll vote data
HSET poll:votes:1 option:1 269 option:2 268 option:3 42
HGETALL poll:votes:1
HINCRBY poll:votes:1 option:1 1
HGET poll:votes:1 option:1
EXPIRE poll:votes:1 300
TTL poll:votes:1
```

## 🎯 Testing Your Application with Redis

### Step 1: Start Redis
```bash
# Choose one method from above
brew services start redis  # macOS
# OR
docker run -d -p 6379:6379 --name redis redis:latest  # Docker
```

### Step 2: Verify Redis is Running
```bash
redis-cli ping
# Should return: PONG
```

### Step 3: Enable Redis in Your Application
```bash
# Add this to your application.properties (create if it doesn't exist)
echo "redis.enabled=true" >> src/main/resources/application.properties
```

### Step 4: Start Your Application
```bash
cd /Users/thoma/Desktop/Dat250.lab
./gradlew bootRun
```

### Step 5: Test the Cache API

Open a new terminal and test the endpoints:

```bash
# 1. Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "pass123", "email": "test@test.com"}'

# 2. Create a poll
curl -X POST "http://localhost:8080/api/polls?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "creatorUserId": 1,
    "question": "Test Poll Question?",
    "publicPoll": true,
    "options": [
      {"caption": "Option 1", "presentationOrder": 1},
      {"caption": "Option 2", "presentationOrder": 2}
    ]
  }'

# 3. Cast a vote
curl -X POST http://localhost:8080/api/polls/1/votes \
  -H "Content-Type: application/json" \
  -d '{"optionId": 1, "voterUserId": 1, "anonymous": false, "isUpvote": true}'

# 4. Get cached vote counts (this uses Redis!)
curl http://localhost:8080/api/polls/1/vote-counts
```

### Step 6: Verify Cache is Working

In Redis CLI, check if your data is cached:
```bash
redis-cli

# Check if poll vote data is cached
KEYS poll:votes:*
HGETALL poll:votes:1
```

## 🔍 Troubleshooting

### Redis Not Starting
```bash
# Check if Redis is already running
ps aux | grep redis

# Check Redis logs (macOS)
brew services restart redis
tail -f /usr/local/var/log/redis.log

# Check Redis logs (Docker)
docker logs redis
```

### Connection Refused Error
```bash
# Make sure Redis is running on port 6379
netstat -an | grep 6379
# OR
lsof -i :6379

# Try connecting with specific host/port
redis-cli -h localhost -p 6379 ping
```

### Application Still Works Without Redis
This is expected! Your application gracefully falls back to database-only mode when Redis isn't available. Check the console output for:
```
Cache service implementation: NoOpCacheService
```

## 📊 Performance Testing

### Test Cache Performance

1. **First request** (cache miss):
```bash
time curl http://localhost:8080/api/polls/1/vote-counts
```

2. **Second request** (cache hit):
```bash
time curl http://localhost:8080/api/polls/1/vote-counts
```

The second request should be significantly faster!

### Monitor Cache in Real-time

```bash
# Open Redis CLI and monitor all commands
redis-cli monitor

# In another terminal, make API calls to see cache operations
curl http://localhost:8080/api/polls/1/vote-counts
```

## 🎉 Success Indicators

You know Redis is working correctly when:

1. ✅ `redis-cli ping` returns `PONG`
2. ✅ Your application starts without errors
3. ✅ Vote count API calls show cached data in Redis CLI
4. ✅ Second API calls are faster than first calls
5. ✅ Redis CLI shows poll data: `HGETALL poll:votes:1`

## 🛑 Stop Redis When Done

```bash
# Stop Redis service (macOS)
brew services stop redis

# Stop Docker container
docker stop redis

# Stop manual Redis server
# Press Ctrl+C in the terminal running redis-server
```

Your Redis cache implementation is now ready for testing and demonstration!
# DAT250 Experiment Assignment 5 - Redis

## Technical Problems Encountered

### Port Conflict Issue
**Problem**: Application failed to start with error "Port 8080 was already in use"
**Solution**: Used `lsof -ti:8080` command to find the process using the port, then killed it with `kill` command

### Redis Connection Timeout
**Problem**: Initial Redis connection had timeout issues
**Solution**: Added timeout configuration in `application.properties`:
spring.data.redis.timeout=2000ms


## Pending Issues

None like issues, but did not add all the optional parts of the expass.



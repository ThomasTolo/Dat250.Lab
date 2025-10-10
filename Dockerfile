# syntax=docker/dockerfile:1

###############################################
# Builder stage: compile and package the app  #
###############################################
FROM gradle:8.10.2-jdk21-alpine AS builder

WORKDIR /home/gradle/src

# Copy Gradle wrapper and build scripts first (better layer caching)
COPY gradle/ gradle/
COPY gradlew settings.gradle.kts build.gradle.kts ./

# Copy sources
COPY src ./src

# Build the Spring Boot fat jar
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew --no-daemon clean bootJar


###############################################
# Runtime stage: slim JRE image               #
###############################################
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy only the built immutable artifact
# The plain jar ends with -plain.jar; we copy the bootable jar (ends with -SNAPSHOT.jar)
COPY --from=builder /home/gradle/src/build/libs/*-SNAPSHOT.jar /app/app.jar

# App port (default for Spring Boot)
EXPOSE 8080

# Run as non-root
USER app

# Helpful defaults so the app in container can reach services on the host when needed
# (You can override these with -e SPRING_DATA_REDIS_HOST=... / -e SPRING_RABBITMQ_HOST=...)
ENV SPRING_DATA_REDIS_HOST=host.docker.internal \
    SPRING_RABBITMQ_HOST=host.docker.internal

# Start the app; limit JVM heap to container memory
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]

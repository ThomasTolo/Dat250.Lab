# syntax=docker/dockerfile:1


FROM gradle:8.10.2-jdk21-alpine AS builder

WORKDIR /home/gradle/src

COPY gradle/ gradle/
COPY gradlew settings.gradle.kts build.gradle.kts ./

COPY src ./src

RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew --no-daemon clean bootJar



FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S app && adduser -S app -G app
WORKDIR /app


COPY --from=builder /home/gradle/src/build/libs/*-SNAPSHOT.jar /app/app.jar


EXPOSE 8080


USER app


ENV SPRING_DATA_REDIS_HOST=host.docker.internal \
    SPRING_RABBITMQ_HOST=host.docker.internal


ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]

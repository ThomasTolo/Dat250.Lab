# DAT250 – Expass 7 (Docker) – Min enkle forklaring

Jeg lærte Docker for første gang i denne oppgaven. Her skriver jeg enkelt hva jeg gjorde, hva som funket, og hva som ikke funket først (og hvordan jeg fikset det).

## Hva jeg skulle gjøre
- Pakke Spring Boot‑appen i en Docker‑container (så den kjører likt hos alle).
- Lage en enkel måte å starte hele «pakken» med Redis og RabbitMQ (Compose).
- Dokumentere det i en kort, forståelig tekst.

## Forberedelser
- Installerte Docker Desktop og sjekket at det kjørte: `docker system info` (ingen feil).
- Bygget prosjektet: `./gradlew clean build -x test`.

## Hva jeg lagde
- `Dockerfile` (multi‑stage):
  - Bruker Gradle‑image (JDK 21) for å bygge jar (`bootJar`).
  - Kopierer bare ferdig jar inn i et lite JRE‑image (Temurin 21).
  - Kjører som ikke‑root bruker.
  - Appen lytter på port 8080.
- `.dockerignore`: for å gjøre bygginga raskere (hopper over build/, node_modules/, osv.).
- `docker-compose.yml`: starter app + Redis + RabbitMQ sammen. Tjenestene snakker internt på nettverket, og jeg eksponerer i utgangspunktet bare appen (8080) til min maskin.

## Slik bygde jeg bildet
Fra prosjektroten (samme mappe som `Dockerfile`):

```sh
docker build -t dat250-lab:latest .
```

## Slik kjørte jeg appen (kun backend)

```sh
docker run --rm -p 8080:8080 dat250-lab:latest
```

Hvis jeg må treffe tjenester på min maskin (Redis/RabbitMQ), kan jeg gi inn miljøvariabler:

```sh
docker run --rm -p 8080:8080 \
  -e SPRING_DATA_REDIS_HOST=host.docker.internal \
  -e SPRING_RABBITMQ_HOST=host.docker.internal \
  dat250-lab:latest
```

## Slik kjørte jeg «alt» med Compose

```sh
docker compose up --build
```

Dette bygger bildet, starter Redis og RabbitMQ, venter til de er «Healthy», og starter appen. Som standard er bare appen synlig fra maskinen min på http://localhost:8080. Redis (6379) og RabbitMQ (5672/15672) er interne, men jeg kan enkelt remappe dem om jeg trenger det:

```yaml
redis:
  ports:
    - "6380:6379"
rabbitmq:
  ports:
    - "5673:5672"     # broker (valgfritt)
    - "15673:15672"   # management UI (valgfritt)
```

Tips: Hvis 8080 allerede er opptatt på maskinen min, kan jeg starte Compose på en annen port:

```sh
APP_PORT=8081 docker compose up --build
```

## Hva jeg sjekket
- App: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:polls`, bruker `sa`, tomt passord)

## Ting som feilet (og hvordan jeg fikset det)
- «port is already allocated» (6379/5672/15672/8080): Jeg sluttet å eksponere disse portene ut av Compose (lot dem være interne), eller jeg remappet til en ledig port (f.eks. 6380, 5673, 15673). Jeg brukte `lsof -i :PORT` for å se hva som brukte porten.
- H2 Console sa at «webAllowOthers er disabled»: Jeg la til `spring.h2.console.settings.web-allow-others=true` i `application.properties`.
- Swagger UI feilet med 500 på `/v3/api-docs`: Jeg fjernet en ekstra springdoc‑dependency (bruker kun `springdoc-openapi-starter-webmvc-ui`). Hvis den fortsatt feiler, kan man tvinge refresh av nettleseren, eller oppgradere/nedgradere springdoc‑versjon til en som passer Spring Boot‑versjonen i prosjektet.

## Hvorfor disse bildene?
- Gradle (JDK 21) som builder: forutsigbart build‑miljø.
- Temurin JRE 21 som runtime: lite og trygt image.

## Hva jeg leverer (Canvas)
- Denne markdown‑fila.
- `Dockerfile`, `.dockerignore`, `docker-compose.yml`.
- Et par skjermbilder fra Docker Desktop og nettleseren som viser at ting kjører.

## Kort oppsummering – hva jeg lærte
Jeg fikk appen inn i en container, lærte hvordan Compose kobler flere tjenester sammen, og hvordan jeg løser vanlige porter‑i‑bruk problemer. Nå kan jeg starte hele stacken på en kommandolinje og alt kjører likt.
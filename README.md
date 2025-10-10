## Prosjekt – Enkel Forklaring (På Norsk)

Målet her er å vise hvordan en liten app kan bygges med flere deler som jobber sammen: backend (Spring Boot), frontend (Svelte), databaser (H2 og Redis), meldinger (RabbitMQ), og kjøring i Docker.

---
## 1. Backend (Spring Boot)
Spring Boot er «motoren» på server‑siden.
- Tar imot HTTP‑kall (f.eks. /api/polls).
- Snakker med databasen for å lagre og hente data om brukere, polls (avstemninger), alternativer og stemmer.
- Sender ut meldinger når noe skjer (ny poll, ny stemme) via RabbitMQ.
- Har WebSocket støtte slik at nettlesere kan få beskjed «live» uten å refreshe.

Hvorfor: Spring Boot gjør det raskt å lage API uten mye oppsett.

---
## 2. Frontend (Svelte + Vite)
Svelte er det brukeren ser og klikker på.
- Viser liste over polls.
- Lar deg stemme og lage nye polls.
- Kan koble til WebSocket for å få oppdateringer med en gang andre stemmer.

Hvorfor: Svelte er lett å lære, og gir rask og liten kode i nettleseren.

---
## 3. H2 Database (SQL i minnet)
H2 er en liten database som kjører i minnet når vi utvikler.
- Lagrer brukere, polls, alternativer og stemmer.
- Forsvinner når appen stoppes.

Hvorfor: Superrask å komme i gang med. Ingen installasjon nødvendig.

---
## 4. Redis (Cache / Hurtiglagring)
Redis kan brukes som et «korttidslager» for ting vi slår opp ofte.
- Kan brukes til å mellomlagre f.eks. stemmetellinger så vi slipper tunge SQL‑spørringer hver gang.
- I dette prosjektet er den lagt inn som støtte dersom vi vil optimalisere.
- Redis brukes som en enkel cache for stemmetellinger: første oppslag henter fra databasen og lagres i Redis; ved stemmeendringer slettes cachet slik at det bygges på nytt neste gang

Hvorfor: Gir fart når mange brukere spør om det samme.

---
## 5. RabbitMQ (Meldingskø)
RabbitMQ er et system for å sende meldinger mellom deler av løsningen.
- Backend publiserer en melding når noen stemmer eller lager en poll.
- Lyttere tar imot meldingen og kan gjøre noe: lagre, sende videre, oppdatere WebSocket.
- Meldingene har «routing key» som f.eks. `poll.3` (poll med id 3).

Hvorfor: Løsner koblingen mellom ting. Andre systemer kan koble seg på senere uten å endre kjernen.   

Enkel flyt:
1. Bruker stemmer i frontend.
2. Frontend kaller backend API.
3. Backend lagrer stemme i DB og publiserer en Vote‑melding til RabbitMQ.
4. Lytter i backend mottar meldingen og kan sende WebSocket oppdatering.
5. Andre tjenester (om vi lager dem senere) kan også høre på samme melding.

---
## 6. Docker
Docker pakker ting slik at det kjører likt overalt.
- RabbitMQ kjører ofte i en Docker‑container.
- Vi kan også pakke backend og frontend i containere senere.

Hvorfor: Mindre «det funker på min maskin men ikke hos deg».

---
## 7. WebSocket (Live oppdatering)
I stedet for at frontend spør hele tiden, kan backend pushe beskjed: «Noen stemte!».
- Når en stemme kommer inn og vi mottar meldingen, sendes et lite JSON‑objekt ut til alle tilkoblete brukere.
- Frontend oppdaterer tallene uten refresh.

---
## 8. Logikken – Hvordan ting henger sammen

Kort beskrivelse av hovedflyten:
- Bruker lager poll -> backend lagrer i DB -> sender PollCreated‑melding -> lytter mottar -> (kan forberede ting / sende info)
- Bruker stemmer -> backend lagrer i DB -> sender Vote‑melding -> lytter mottar -> sender WebSocket oppdatering -> frontend viser nytt stemmetall 

Nøkkelidé: Vi separerer «hendelse skjer» (publisere melding) fra «hva vi gjør med det» (lytter). Dette gjør det lett å legge til nye reaksjoner senere (f.eks. statistikk‑tjeneste) uten å endre kjernefunksjonene.

---
## 9. Hvorfor denne arkitekturen?
- Lett å bytte ut én del (f.eks. bytte H2 med Postgres) uten å fjerne resten.
- Meldinger via RabbitMQ gjør systemet mer fleksibelt og skalerbart.
- Frontend og backend kan utvikles separat.
- WebSocket gir live følelse.
- Cache (Redis) kan gi fart når trafikken øker.

---
## 10. Hvordan kjøre (enkelt)
Du kan kjøre appen på to måter: vanlig (lokalt) eller med Docker/Compose.

### A) Lokalt
1. Start RabbitMQ (f.eks. med Docker image `rabbitmq:3.13-management`).
2. Kjør backend:

	```sh
	./gradlew bootRun
	```

3. Kjør frontend (inne i `Frontend/`):

	```sh
	npm install
	npm run dev
	```

4. Åpne nettleser (f.eks. http://localhost:5173).

### B) Med Docker/Compose (anbefalt for lik oppsett)
1. Bygg og start alt:

	```sh
	docker compose up --build
	```

2. Besøk:
	- App: http://localhost:8080
	- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:polls`, bruker `sa`)


3. Stoppe alt:

	```sh
	docker compose down
	```

Test meldinger uten CLI:
- Publiser stemme: POST til /admin/publish/vote
- Se køstatus: GET /admin/queues/poll-events

## 11. Videre forbedringer (idéer)
- Legge på ekte database (Postgres/MySQL).
- Legge på sikkerhet (innlogging, roller).
- Egen tjeneste for analyser av stemmer.

---
## 12. Ordliste (enkle ord)
- API: Dør inn til backend (URL + data).
- Queue (kø): Liste av meldinger som venter.
- Meldingsbroker: Postkontor for meldinger.
- Event / Hendelse: «Noe har skjedd» beskjed.
- Cache: Raskt midlertidig lager.
- Container: Pakke som kjører likt overalt.

---
## 13. Kort oppsummert
Frontend (Svelte) snakker med Backend (Spring Boot). Backend lagrer i H2 og sender meldinger til RabbitMQ. Lyttere tar imot og oppdaterer brukere via WebSocket. Redis kan hjelpe med fart. Docker gjør det lett å kjøre alt likt. Alt er delt opp slik at vi kan vokse senere.

Lykke til videre – spør gjerne om du vil ha mer detaljer om én del!

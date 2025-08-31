DAT250 – Experiment Assignment 2
Author: Thomas Tolo Jensen
File: dat250-expass2.md

1. Overview
This assignment was to build a small Poll/Quiz application with Spring Boot, expose a REST API, create manual test scenarios (HTTP client), convert them into automated tests, and finally set up CI with GitHub Actions.
This report summarizes what I implemented, how I tested it, and—most importantly—the technical problems I hit and how I solved them.

2. What I built
Domain model
User – username, password, email, lists of created poll IDs and vote IDs.
Poll – question, publishedAt, validUntil, public/private flags, optional maxVotesPerUser, invited usernames, options, and vote IDs.
VoteOption – caption, presentationOrder, (id and pollId).
Vote – optionId, voterUserId, publishedAt, anonymous flag.
Quiz – specialization/meta of Poll (private poll with correct options & scoring planned as an extension).
All domain classes are simple Java Beans (no heavy logic) with getters/setters and no-args constructors.
In-memory service
PollManager (@Component) – holds users, polls and votes in HashMap<UUID, …>, generates IDs, and enforces minimal rules (e.g., updating a vote replaces previous vote by same user/poll).
Web/API
UserController – POST /api/users, GET /api/users.
PollController – POST /api/polls, GET /api/polls, DELETE /api/polls/{id}.
VoteController – POST /api/polls/{pollId}/votes, GET /api/polls/{pollId}/votes.
ApiExceptionHandler – returns clean JSON for common errors (404, bad request, etc.).
HomeController – redirects / → /swagger-ui.html.
Project layout:

src/
  main/
    java/no/hvl/Lab1/
      Domain/ (User, Poll, Vote, VoteOption, Quiz)
      Service/ (PollManager)
      Web/ (UserController, PollController, VoteController, ApiExceptionHandler, HomeController)
    resources/
      application.properties
  test/
    java/no/hvl/Lab1/PollScenarioTest.java
    resources/http/PollScenarios.http

3. Manual test scenarios (Step 3)
I used the VS Code REST Client to create PollScenarios.http that exercises the full flow:
Create user 1 → capture {{u1Id}}
List users → contains user 1
Create user 2 → capture {{u2Id}}
List users → contains both users
User 1 creates poll → capture {{pollId}}, and {{redOptionId}}, {{blueOptionId}} from the response
User 2 votes red
User 2 changes vote to blue
List votes → shows latest vote for user 2
Delete poll
List votes → empty
Gotcha: the REST Client scripting uses JavaScript blocks. I had to parse the JSON response and set variables:
> {%
  const body = JSON.parse(response.body);
  client.global.set("pollId", body.id);
  const red = body.options.find(o => o.caption === "Red");
  if (red) client.global.set("redOptionId", red.id);
%}

4. Automated tests (Step 5/6)
I converted the scenario into a JUnit 5 test using Spring Boot.
Final approach: @SpringBootTest + MockMvc (I also tried RestClient in between).
The test creates users, polls, performs votes, asserts the latest vote, and ensures votes disappear after deleting the poll.
Key assertions check response codes (201 Created for creates and 200 OK for lists) and parse the JSON bodies using Jackson’s ObjectMapper.

5. API documentation (Step 6 – optional)
I enabled springdoc-openapi so /swagger-ui.html shows API docs.
Working configuration:
Spring Boot 3.4.5
springdoc-openapi-starter-webmvc-ui 2.6.0
Added spring-boot-starter-validation (fixes validator provider warning)
application.properties (relevant lines):
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.packages-to-scan=no.hvl.Lab1.Web
springdoc.paths-to-match=/api/**
I also added a simple /api/ping endpoint when debugging to confirm springdoc was able to build the docs before turning it back on for all controllers.

6. CI build automation (Step 7)
I created a GitHub Actions workflow .github/workflows/ci.yml that:
Checks out the repo
Sets up JDK 21
Validates Gradle wrapper
Runs ./gradlew clean test
Publishes JUnit summary and uploads the HTML report as an artifact
The final workflow includes:
permissions:
  contents: read
  checks: write
This fixed a permission error when the dorny/test-reporter action tried to publish results.

7. Problems & how I solved them
7.1. Swagger/OpenAPI returned HTTP 500 (/v3/api-docs)
Symptom: Swagger UI showed “Failed to load API definition. response status is 500 /v3/api-docs”.
Console error: NoSuchMethodError: ControllerAdviceBean.<init>(Object)
Cause: Version mismatch – I was on Spring Boot 3.5.x while using springdoc 2.6.0, which still targets Framework 6.1 (Boot 3.2).
Fix:
Downgraded to Spring Boot 3.2.5, kept springdoc at 2.6.0, and removed the extra springdoc-openapi-starter-webmvc-api dependency.
Added spring-boot-starter-validation.
Verified /v3/api-docs returns JSON, then opened /swagger-ui.html.

7.2. Silent typo in application.properties
Symptom: Docs still failing after fixing versions.
Cause: I had appended text on the same line:
springdoc.paths-to-match=/api/** is this the mistake
That string became the actual value.
Fix: Keep the property value exactly /api/** and move comments to a new line or prefix with #.

7.3. Port 8080 already in use
Symptom: Spring Boot couldn’t start.
Fix: Killed the process, or ran with server.port=8081 temporarily.

7.4. REST Client variables not set
Symptom: Later requests used "{{pollId}}" literally.
Cause: I forgot to capture IDs after responses.
Fix: Added JavaScript capture blocks to PollScenarios.http.

7.5. HTTP 400 when creating a poll from REST file
Symptom: Cannot deserialize value of type 'java.util.UUID' from String "{{u1Id}}".
Cause: I ran “Create Poll” before “Create User 1”; {{u1Id}} wasn’t set.
Fix: Always execute blocks in order; also added quick “List” steps to verify captured variables.

7.6. JUnit status mismatch (201 vs 200)
Symptom: Test expected 201 Created but controller returned 200 OK.
Fix: Standardized controllers to return 201 Created on POST (or adjusted test to accept either when appropriate).

7.7. GitHub Actions error: “Resource not accessible by integration”
Symptom: CI failed at “Publish JUnit results”.
Cause: The workflow’s GITHUB_TOKEN lacked permissions to create a check run.
Fix: Added:
permissions:
  contents: read
  checks: write
Run succeeded; artifact test-report is produced.

8. Pending / possible improvements
Quiz extension: scoring by correctness and response time; leaderboard aggregation.
Validation & errors: more robust DTO validation, clearer 4xx messages, and coverage for edge cases: anonymous multi-vote on public polls, time-window voting rules, etc.
Persistent storage: swap HashMap store for JPA and a database.
DTOs for docs: use response/request DTOs to avoid circular references and make Swagger schemas cleaner.
More tests: add negative tests (deadline passed, wrong poll/option IDs, etc.).

9. How to run locally
./gradlew bootRun
# UI:
#   http://localhost:8080/swagger-ui.html
# Raw docs:
#   http://localhost:8080/v3/api-docs
Run tests:
./gradlew test
# HTML report: build/reports/tests/test/index.html

10. Conclusion
I implemented the Poll API with an in-memory domain model, built controllers, created both manual REST Client scenarios and JUnit automated tests, and set up GitHub Actions to run tests and publish reports. The main learning outcome was troubleshooting version compatibility (Spring Boot vs springdoc) and catching small configuration mistakes that cascade into hard-to-read 500 errors. After aligning versions, fixing properties, and adding the right CI permissions, everything runs green end-to-end.
DAT250 – Experiment Assignment 2
Thomas Tolo Jensen

**Report**

1. 
In this assignment (expass2) I buildt a small Poll/Quiz application with Spring Boot, expose a REST API, manual test scenarios (using Visual studio code HTTP client), convert them into automated tests in java, and finally set up CI with GitHub Actions.
Here i will summarizes what I implemented, how I tested it, technical problems I got and how I solved them.

2. What I built (Step 2 and 4)
Domain model:
User containing username, password, email, lists of created poll IDs and vote IDs.
Poll containing question, publishedAt, validUntil, public/private flags, optional maxVotesPerUser, invited usernames, options, and vote IDs.
VoteOption containing caption, presentationOrder, (id and pollId).
Vote containing optionId, voterUserId, publishedAt, anonymous flag.
Quiz containing specialization/meta of Poll (private poll with correct options & scoring planned as an extension).

In-memory service:
PollManager (@Component) – holds users, polls and votes in HashMap, generates IDs

Web/API:
UserController,  with POST and GET methods
PollController, with POST, GET and DELETE methods
VoteController, POST and GET methids
ApiExceptionHandler which returns clean JSON for common errors (I added this class for myself to help with debugging as a few problems araised during this weeks Lab. Which I will mention later)



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

4. Automated tests (Step 5)
I converted the scenario into a JUnit 5 test using Spring Boot.
Final approach: @SpringBootTest + MockMvc (I also tried RestClient in between).
The test creates users, polls, performs votes, asserts the latest vote, and ensures votes disappear after deleting the poll.
** Key assertions check response codes (201 Created for creates and 200 OK for lists) and parse the JSON bodies using Jackson’s ObjectMapper. **

5. API documentation (Step 6 – optional)
I enabled springdoc-openapi so /swagger-ui.html shows API docs.
Working configuration:
Spring Boot 3.2.5
springdoc-openapi-starter-webmvc-ui 2.6.0
Added spring-boot-starter-validation (fixes validator provider warning)
application.properties (relevant lines):
springdoc.swagger-ui.path=/swagger-ui.html


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

7.2. Port 8080 already in use
Symptom: Spring Boot couldn’t start.
Fix: Killed the process using, Lsof -i :8080 and using kill command to end the active session.

7.3. REST Client variables not set
Symptom: Later requests used "{{pollId}}" literally.
Cause: I forgot to capture IDs after responses.
Fix: Added JavaScript capture blocks to PollScenarios.http.

7.4. HTTP 400 when creating a poll from REST file
Symptom: Cannot deserialize value of type 'java.util.UUID' from String "{{u1Id}}".
Cause: I ran “Create Poll” before “Create User 1”; {{u1Id}} wasn’t set.
Fix: Always execute blocks in order; also added quick “List” steps to verify captured variables.

7.5. JUnit status mismatch (201 vs 200)
Symptom: Test expected 201 Created but controller returned 200 OK.
Fix: Standardized controllers to return 201 Created on POST 

7.6. GitHub Actions error: “Resource not accessible by integration”
Symptom: CI failed at “Publish JUnit results”.
Cause: The workflow’s GITHUB_TOKEN lacked permissions to create a check run.
Fix: Added:
permissions:
  contents: read
  checks: write
Run succeeded; artifact test-report is produced.

8. Improvements
Quiz extension: scoring by correctness and response time; leaderboard aggregation.
Validation & errors: more robust DTO validation, clearer 4xx messages, and coverage for edge cases: anonymous multi-vote on public polls, time-window voting rules, etc.
Persistent storage: swap HashMap store for JPA and a database.
DTOs for docs: use response/request DTOs to avoid circular references and make Swagger schemas cleaner.
More tests: add negative tests (deadline passed, wrong poll/option IDs, etc.).


10. Conclusion
I implemented the Poll API with an in-memory domain model, built controllers, created both manual REST Client scenarios and JUnit automated tests, and set up GitHub Actions to run tests and publish reports. The main learning outcome was troubleshooting version compatibility (Spring Boot vs springdoc) and catching small configuration mistakes that cascade into hard-to-read 500 errors. After aligning versions, fixing properties, and adding the right CI permissions, everything runs green end-to-end.
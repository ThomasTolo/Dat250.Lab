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

I used the VS Code REST Client to script a full test flow in my file PollScenarios.http.
This is what it does:
Create two users and confirm they appear in the list.
User 1 creates a poll with red/blue options.
User 2 votes red, then changes to blue.
Listing votes shows only the latest vote.
After deleting the poll, votes are gone.

4. Automated tests (Step 5)
I converted the scenario into a JUnit test using Spring Boot.
By using @SpringBootTest + MockMvc (https://www.youtube.com/watch?v=M8iml7gF6ZU) I used this video to help getting started.
The test creates users, polls, performs votes, asserts the latest vote, and ensures votes disappear after deleting the poll.


5. API documentation (Step 6)
I enabled API documentation /swagger-ui.html shows API docs. 
https://springdoc.org spent some time in this web page to get famliar with the library



6. CI build automation (Step 7)
I set up a GitHub Actions workflow (.github/workflows/ci.yml) to automatically build and test the project. It checks out the code, installs JDK 21, validates the Gradle wrapper, and runs the tests. After that, it publishes a JUnit summary and uploads the HTML test report. 
I found this video very helpful: https://www.youtube.com/watch?v=n-UMi4_ppDk, and also used https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-gradle to get familier with githubs CI for java + gradle.

7. Problems & how I solved them

7.1. Swagger/OpenAPI returned HTTP 500 
Issue: 
Swagger UI showed “Failed to load API definition. response status is 500.
Cause: 
Version mismatch.
Fix:
I needed to downgrade to Spring Boot 3.2.5, kept springdoc at 2.6.0.


7.2. Port 8080 already in use
Spring Boot couldn’t start.
Fix: 
Killed the process using, Lsof -i :8080 and using kill command to end the active session.

7.3. REST Client variables not set
Code from test requests used "{{pollId}}" literally.
Cause: 
I forgot to capture IDs after responses.
Fix: 
Added JavaScript capture blocks to PollScenarios.http.

7.4. HTTP 400 when creating a poll from REST file
I got this error "Cannot deserialize value of type 'java.util.UUID' from String "{{u1Id}}"."
Cause:
I ran “Create Poll” before “Create User 1”; {{u1Id}} wasn’t set.
Fix: 
Always execute blocks in order; also added quick “List” steps to verify captured variables.

7.5. GitHub Actions error: “Resource not accessible by integration”
CI failed at “Publish JUnit results”.
Cause: The workflow’s GITHUB_TOKEN lacked permissions to create a check run.
Fix: 
Added:
permissions:
  contents: read
  checks: write
Run succeeded;

8. Improvements
Quiz extension: more questions to choose and score points based off.
More tests: deadline passed, wrong poll/option IDs, etc.


9. Conclusion
I implemented the Poll API with an in-memory domain model, built controllers, created both manual REST Client scenarios and JUnit automated tests, and set up GitHub Actions to run tests and publish reports. The main learning outcome was getting familiar with spring boot, which I have never used before DAT250, I have spent a lot of time getting familiar with the program. It has also been a good task to improve troubleshooting and testing code.
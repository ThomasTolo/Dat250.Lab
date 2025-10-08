package no.hvl.Lab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

//Source: https://www.youtube.com/watch?v=M8iml7gF6ZU

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PollScenarioTest {

    @LocalServerPort int port;

    @Autowired TestRestTemplate http;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private static void assert2xx(ResponseEntity<?> res, String msg) {
        assertTrue(res.getStatusCode().is2xxSuccessful(), msg + " | got: " + res.getStatusCode());
    }

    private <T> ResponseEntity<T> postJson(String path, Object body, Class<T> type) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return http.postForEntity(url(path), new HttpEntity<>(body, h), type);
    }

    private <T> ResponseEntity<T> get(String path, ParameterizedTypeReference<T> type) {
        return http.exchange(url(path), HttpMethod.GET, HttpEntity.EMPTY, type);
    }

    private ResponseEntity<Void> delete(String path) {
        return http.exchange(url(path), HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
    }

    // ----------------- test -----------------

    @Test
    public void scenario_shouldWorkWith200or201() {
        // Create user 1
        Map<String,Object> u1Req = Map.of("username","u1","password","pw1","email","u1@example.com");
        ResponseEntity<Map> u1Res = postJson("/api/users", u1Req, Map.class);
        assert2xx(u1Res, "create user 1");
        String u1Id = (String) Objects.requireNonNull(u1Res.getBody()).get("id");
        assertNotNull(u1Id);

        // List users -> 1
        ResponseEntity<List<Map<String,Object>>> users1 =
                get("/api/users", new ParameterizedTypeReference<>() {});
        assert2xx(users1, "list users after u1");
        assertEquals(1, Objects.requireNonNull(users1.getBody()).size());

        // Create user 2
        Map<String,Object> u2Req = Map.of("username","u2","password","pw2","email","u2@example.com");
        ResponseEntity<Map> u2Res = postJson("/api/users", u2Req, Map.class);
        assert2xx(u2Res, "create user 2");
        String u2Id = (String) Objects.requireNonNull(u2Res.getBody()).get("id");
        assertNotNull(u2Id);

        // List users -> 2
        ResponseEntity<List<Map<String,Object>>> users2 =
                get("/api/users", new ParameterizedTypeReference<>() {});
        assert2xx(users2, "list users after u2");
        assertEquals(2, Objects.requireNonNull(users2.getBody()).size());

        // Create poll by user 1
        Map<String,Object> pollReq = new LinkedHashMap<>();
        pollReq.put("creatorUserId", u1Id);
        pollReq.put("question", "Your favorite color?");
        pollReq.put("publicPoll", true);
        pollReq.put("publishedAt", Instant.parse("2025-01-01T00:00:00Z"));
        pollReq.put("validUntil", Instant.parse("2026-01-01T00:00:00Z"));
        pollReq.put("options", List.of(
                Map.of("caption","Red",   "presentationOrder", 1),
                Map.of("caption","Blue",  "presentationOrder", 2),
                Map.of("caption","Green", "presentationOrder", 3)
        ));

        ResponseEntity<Map> pollRes = postJson("/api/polls?userId=" + u1Id, pollReq, Map.class);
        assert2xx(pollRes, "create poll");
        Map poll = Objects.requireNonNull(pollRes.getBody());
        String pollId = (String) poll.get("id");
        assertNotNull(pollId);

        @SuppressWarnings("unchecked")
        List<Map<String,Object>> options = (List<Map<String,Object>>) poll.get("options");
        assertNotNull(options);
        assertEquals(3, options.size());

        String redOptionId = options.stream()
                .filter(o -> "Red".equals(o.get("caption")))
                .map(o -> (String) o.get("id"))
                .findFirst().orElseThrow();
        String blueOptionId = options.stream()
                .filter(o -> "Blue".equals(o.get("caption")))
                .map(o -> (String) o.get("id"))
                .findFirst().orElseThrow();

        // List polls -> 1
        ResponseEntity<List<Map<String,Object>>> pollsRes =
                get("/api/polls", new ParameterizedTypeReference<>() {});
        assert2xx(pollsRes, "list polls");
        assertEquals(1, Objects.requireNonNull(pollsRes.getBody()).size());

        // User 2 votes (Red)
        Map<String,Object> voteRed = Map.of("optionId", redOptionId, "voterUserId", u2Id, "anonymous", false);
        ResponseEntity<Map> voteRedRes = postJson("/api/polls/" + pollId + "/votes", voteRed, Map.class);
        assert2xx(voteRedRes, "vote red");

        // User 2 changes vote (Blue)
        Map<String,Object> voteBlue = Map.of("optionId", blueOptionId, "voterUserId", u2Id, "anonymous", false);
        ResponseEntity<Map> voteBlueRes = postJson("/api/polls/" + pollId + "/votes", voteBlue, Map.class);
        assert2xx(voteBlueRes, "vote blue");

        // List votes -> latest for u2 should be Blue (some impls return only latest; others all)
        ResponseEntity<List<Map<String,Object>>> votesRes =
                get("/api/polls/" + pollId + "/votes", new ParameterizedTypeReference<>() {});
        assert2xx(votesRes, "list votes");
        List<Map<String,Object>> votes = Objects.requireNonNull(votesRes.getBody());
        assertFalse(votes.isEmpty());
                // pick the latest vote by u2 (by publishedAt) and assert it's blue
                Map<String,Object> lastByU2 = votes.stream()
                        .filter(v -> u2Id.equals(v.get("voterUserId")))
                        .max(Comparator.comparing(v -> Instant.parse((String)v.get("publishedAt"))))
                        .orElse(null);
                assertNotNull(lastByU2, "Should have a vote from user2");
                assertEquals(blueOptionId, lastByU2.get("optionId"));

        // Delete poll (allow 200/204)
        ResponseEntity<Void> delRes = delete("/api/polls/" + pollId);
        assertTrue(delRes.getStatusCode().is2xxSuccessful(),
                "delete poll should be 2xx, got: " + delRes.getStatusCode());

        // Votes after delete -> should be 200 with empty []
        ResponseEntity<List<Map<String,Object>>> afterVotes =
                get("/api/polls/" + pollId + "/votes", new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.OK, afterVotes.getStatusCode(), "votes endpoint should return 200 after poll deletion");
        assertTrue(Objects.requireNonNull(afterVotes.getBody()).isEmpty(),
                "votes should be empty after poll deletion");
    }
}

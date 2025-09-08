
package no.hvl.Lab.Web;

import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import no.hvl.Lab.Domain.Quiz;
import no.hvl.Lab.Domain.QuizSession;
import no.hvl.Lab.Domain.User;
import no.hvl.Lab.Domain.VoteOption;
import no.hvl.Lab.Service.PollManager;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    private final PollManager manager;
    public QuizController(PollManager manager) { this.manager = manager; }

    @PostMapping
    public Quiz createQuiz(@RequestBody CreateQuizRequest req) {
        return manager.createQuiz(
                req.creatorUserId,
                req.question,
                req.publishedAt,
                req.validUntil,
                req.invitedUsernames,
                req.options,
                req.correctOptionIds
        );
    }

    @GetMapping
    public Collection<Quiz> listQuizzes() { return manager.allQuizzes(); }

    @GetMapping("/{quizId}")
    public Quiz get(@PathVariable UUID quizId) {
        // Not implemented: findQuiz
        return null;
    }

    @PutMapping("/{quizId}")
    public Quiz update(@PathVariable UUID quizId, @RequestBody CreateQuizRequest req) {
        // Not implemented: updateQuiz
        return null;
    }

    @DeleteMapping("/{quizId}")
    public void delete(@PathVariable UUID quizId) {
        // Not implemented: deleteQuiz
    }

    @PostMapping("/session")
    public QuizSession createSession(@RequestBody CreateSessionRequest req) {
        return manager.createQuizSession(req.name, req.questions, req.invitedUsernames);
    }

    @GetMapping("/session/{sessionId}/leaderboard")
    public ResponseEntity<?> getSessionLeaderboard(@PathVariable UUID sessionId) {
        List<Map.Entry<UUID, Integer>> leaderboard = manager.getQuizSessionLeaderboard(sessionId);
        Map<UUID, User> users = manager.allUsers().stream().collect(Collectors.toMap(User::getId, u -> u));
        List<Map<String, Object>> result = leaderboard.stream()
                .map(entry -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("userId", entry.getKey());
                    m.put("username", users.getOrDefault(entry.getKey(), new User()).getUsername());
                    m.put("points", entry.getValue());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    public static class CreateQuizRequest {
        public UUID creatorUserId;
        public String question;
        public Instant publishedAt;
        public Instant validUntil;
        public Set<String> invitedUsernames;
        public List<VoteOption> options;
        public Set<UUID> correctOptionIds;
    }

    public static class CreateSessionRequest {
        public String name;
        public List<Quiz> questions;
        public Set<String> invitedUsernames;
    }
}

package no.hvl.Lab1.Service;

import no.hvl.Lab1.Domain.*;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * In-memory repository / domain manager for Users, Polls, Options and Votes.
 * This is intentionally simple for the first draft (no persistence).
 */
@Component
public class PollManager {

    private final Map<UUID, User> users = new HashMap<>();
    private final Map<UUID, Poll> polls = new HashMap<>();
    private final Map<UUID, Vote> votes = new HashMap<>();

    // -------- Users --------
    public User registerUser(String username, String password, String email) {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername(username);
        u.setPassword(password);
        u.setEmail(email);
        users.put(u.getId(), u);
        return u;
    }

    public Optional<User> findUser(UUID id) { return Optional.ofNullable(users.get(id)); }
    public Collection<User> allUsers() { return users.values(); }

    // -------- Polls --------
    public Poll createPoll(UUID creatorUserId,
                           String question,
                           boolean isPublic,
                           Instant publishedAt,
                           Instant validUntil,
                           Integer maxVotesPerUser,
                           Collection<String> invitedUsernames,
                           Collection<VoteOption> options) {

        Poll p = new Poll();
        p.setId(UUID.randomUUID());
        p.setCreatorUserId(creatorUserId);
        p.setQuestion(question);
        p.setPublicPoll(isPublic);
        p.setPublishedAt(publishedAt);
        p.setValidUntil(validUntil);
        p.setMaxVotesPerUser(maxVotesPerUser);
        if (invitedUsernames != null) p.getInvitedUsernames().addAll(invitedUsernames);
        if (options != null) {
            for (VoteOption vo : options) {
                if (vo.getId() == null) vo.setId(UUID.randomUUID());
                vo.setPollId(p.getId());
                p.getOptions().add(vo);
            }
            p.getOptions().sort(Comparator.comparingInt(VoteOption::getPresentationOrder));
        }
        polls.put(p.getId(), p);

        // Link back to user (if we have them)
        User creator = users.get(creatorUserId);
        if (creator != null) creator.getCreatedPollIds().add(p.getId());

        return p;
    }

    public Optional<Poll> findPoll(UUID id) { return Optional.ofNullable(polls.get(id)); }
    public Collection<Poll> allPolls() { return polls.values(); }

    // -------- Votes --------
    public Vote castVote(UUID pollId, UUID optionId, UUID voterUserId, boolean anonymous) {
        Poll poll = polls.get(pollId);
        if (poll == null) throw new NoSuchElementException("Poll not found: " + pollId);

        Vote v = new Vote();
        v.setId(UUID.randomUUID());
        v.setPollId(pollId);
        v.setOptionId(optionId);
        v.setVoterUserId(voterUserId);
        v.setAnonymous(anonymous);
        v.setPublishedAt(Instant.now());

        votes.put(v.getId(), v);
        poll.getVoteIds().add(v.getId());

        if (voterUserId != null) {
            User u = users.get(voterUserId);
            if (u != null) u.getVoteIds().add(v.getId());
        }
        return v;
    }

    public Optional<Vote> findVote(UUID id) { return Optional.ofNullable(votes.get(id)); }

    // Convenience queries
    public List<Poll> publicPolls() {
        List<Poll> res = new ArrayList<>();
        for (Poll p : polls.values()) if (p.isPublicPoll()) res.add(p);
        return res;
    }

    public List<Poll> privatePollsVisibleTo(String username) {
        List<Poll> res = new ArrayList<>();
        for (Poll p : polls.values()) {
            if (!p.isPublicPoll() && (p.getInvitedUsernames().contains(username))) {
                res.add(p);
            }
        }
        return res;
    }
    // delete a poll and its votes
public void deletePoll(UUID pollId) {
    Poll p = polls.remove(pollId);
    if (p == null) return;
    // remove votes tied to this poll
    for (UUID vId : p.getVoteIds()) {
        Vote v = votes.remove(vId);
        if (v != null && v.getVoterUserId() != null) {
            User u = users.get(v.getVoterUserId());
            if (u != null) u.getVoteIds().remove(vId);
        }
    }
    // unlink from creator
    if (p.getCreatorUserId() != null) {
        User creator = users.get(p.getCreatorUserId());
        if (creator != null) creator.getCreatedPollIds().remove(pollId);
    }
}

// change vote = keep only the newest vote per user for listing
public Vote castOrChangeVote(UUID pollId, UUID optionId, UUID voterUserId, boolean anonymous) {
    return castVote(pollId, optionId, voterUserId, anonymous); // simple: create new record
}

// latest per user (deduplicate by voterUserId, keep most recent)
public List<Vote> votesForPollLatestPerUser(UUID pollId) {
    Map<UUID, Vote> latest = new HashMap<>();
    Poll p = polls.get(pollId);
    if (p == null) return List.of();
    for (UUID vId : p.getVoteIds()) {
        Vote v = votes.get(vId);
        if (v == null) continue;
        UUID key = v.getVoterUserId(); // null (anonymous) will collapse all anon into one; OK for demo
        Vote prev = latest.get(key);
        if (prev == null || (v.getPublishedAt() != null && v.getPublishedAt().isAfter(prev.getPublishedAt()))) {
            latest.put(key, v);
        }
    }
    return new ArrayList<>(latest.values());
}

// -------- Quizzes --------
private final Map<UUID, Quiz> quizzes = new HashMap<>();
private final Map<UUID, QuizSession> quizSessions = new HashMap<>();
private final Map<UUID, Map<UUID, Integer>> quizPoints = new HashMap<>(); // quizId -> (userId -> points)

public Quiz createQuiz(UUID creatorUserId,
                      String question,
                      Instant publishedAt,
                      Instant validUntil,
                      Set<String> invitedUsernames,
                      List<VoteOption> options,
                      Set<UUID> correctOptionIds) {
    Quiz quiz = new Quiz();
    quiz.setId(UUID.randomUUID());
    quiz.setCreatorUserId(creatorUserId);
    quiz.setQuestion(question);
    quiz.setPublishedAt(publishedAt);
    quiz.setValidUntil(validUntil);
    if (invitedUsernames != null) quiz.getInvitedUsernames().addAll(invitedUsernames);
    if (options != null) quiz.setOptions(options);
    if (correctOptionIds != null) quiz.setCorrectOptionIds(correctOptionIds);
    quizzes.put(quiz.getId(), quiz);
    return quiz;
}

public Collection<Quiz> allQuizzes() { return quizzes.values(); }

public QuizSession createQuizSession(String name, List<Quiz> questions, Set<String> invitedUsernames) {
    QuizSession session = new QuizSession();
    session.setName(name);
    session.setQuestions(questions);
    if (invitedUsernames != null) session.getInvitedUsernames().addAll(invitedUsernames);
    quizSessions.put(session.getId(), session);
    return session;
}

public List<Map.Entry<UUID, Integer>> getQuizSessionLeaderboard(UUID sessionId) {
    QuizSession session = quizSessions.get(sessionId);
    if (session == null) return List.of();
    Map<UUID, Integer> totalScores = new HashMap<>();
    for (Quiz quiz : session.getQuestions()) {
        Map<UUID, Integer> scores = quizPoints.getOrDefault(quiz.getId(), Map.of());
        for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
            totalScores.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
    }
    List<Map.Entry<UUID, Integer>> leaderboard = new ArrayList<>(totalScores.entrySet());
    leaderboard.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
    return leaderboard;
}

// Cast a vote for a quiz, enforce rules and award points
public Vote castQuizVote(UUID quizId, UUID optionId, UUID voterUserId) {
    Quiz quiz = quizzes.get(quizId);
    if (quiz == null) throw new NoSuchElementException("Quiz not found: " + quizId);
    Instant now = Instant.now();
    // 1. Check time window
    if (quiz.getPublishedAt() != null && now.isBefore(quiz.getPublishedAt()))
        throw new IllegalStateException("Quiz not yet published");
    if (quiz.getValidUntil() != null && now.isAfter(quiz.getValidUntil()))
        throw new IllegalStateException("Quiz deadline passed");
    // 2. Private quiz: check invite
    if (voterUserId == null)
        throw new IllegalStateException("Quiz: must be logged in");
    User user = users.get(voterUserId);
    if (user == null || !quiz.getInvitedUsernames().contains(user.getUsername()))
        throw new IllegalStateException("User not invited to quiz");
    // 3. Only one vote per user per quiz
    long userVotes = quiz.getVoteIds().stream()
            .map(votes::get)
            .filter(v -> v != null && voterUserId.equals(v.getVoterUserId()))
            .count();
    if (userVotes >= 1)
        throw new IllegalStateException("Vote limit reached for user");
    // 4. Register vote
    Vote v = new Vote();
    v.setId(UUID.randomUUID());
    v.setPollId(quizId); // quizId used as pollId for votes
    v.setOptionId(optionId);
    v.setVoterUserId(voterUserId);
    v.setAnonymous(false);
    v.setPublishedAt(now);
    votes.put(v.getId(), v);
    quiz.getVoteIds().add(v.getId());
    user.getVoteIds().add(v.getId());
    // 5. Award points if correct
    if (quiz.getCorrectOptionIds().contains(optionId)) {
        long timeTaken = now.toEpochMilli() - quiz.getPublishedAt().toEpochMilli();
        int points = Math.max(1, 10000 - (int) timeTaken / 1000); // Example: faster = more points
        quizPoints.computeIfAbsent(quizId, k -> new HashMap<>())
                .merge(voterUserId, points, Integer::sum);
    }
    return v;
}

}
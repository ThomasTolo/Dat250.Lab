package no.hvl.Lab1.Domain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A Quiz is a special private poll with correct options and scoring.
 * (Logic for scoring/leaderboard can come later.)
 */
public class Quiz extends Poll {
    private Set<UUID> correctOptionIds = new HashSet<>();

    public Quiz() {
        setPublicPoll(false);            // quizzes are private
        setMaxVotesPerUser(1);           // one vote per user by default
    }

    public Set<UUID> getCorrectOptionIds() { return correctOptionIds; }
    public void setCorrectOptionIds(Set<UUID> correctOptionIds) { this.correctOptionIds = correctOptionIds; }
}


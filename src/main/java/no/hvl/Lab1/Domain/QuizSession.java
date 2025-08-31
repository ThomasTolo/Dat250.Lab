package no.hvl.Lab1.Domain;

import java.util.*;

/**
 * A QuizSession groups multiple Quiz questions and tracks cumulative scores.
 */
public class QuizSession {
    private UUID id;
    private String name;
    private List<Quiz> questions = new ArrayList<>();
    private Set<String> invitedUsernames = new HashSet<>();

    public QuizSession() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Quiz> getQuestions() { return questions; }
    public void setQuestions(List<Quiz> questions) { this.questions = questions; }
    public Set<String> getInvitedUsernames() { return invitedUsernames; }
    public void setInvitedUsernames(Set<String> invitedUsernames) { this.invitedUsernames = invitedUsernames; }
}

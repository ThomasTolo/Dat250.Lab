package no.hvl.Lab1.Domain;

import java.time.Instant;
import java.util.*;

/**
 * A Quiz is a standalone entity, not a Poll.
 */
public class Quiz {
    private UUID id;
    private String question;
    private Instant publishedAt;
    private Instant validUntil;
    private Set<String> invitedUsernames = new HashSet<>();
    private UUID creatorUserId;
    private List<VoteOption> options = new ArrayList<>();
    private Set<UUID> voteIds = new HashSet<>();
    private Set<UUID> correctOptionIds = new HashSet<>();

    public Quiz() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }

    public Set<String> getInvitedUsernames() { return invitedUsernames; }
    public void setInvitedUsernames(Set<String> invitedUsernames) { this.invitedUsernames = invitedUsernames; }

    public UUID getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(UUID creatorUserId) { this.creatorUserId = creatorUserId; }

    public List<VoteOption> getOptions() { return options; }
    public void setOptions(List<VoteOption> options) { this.options = options; }

    public Set<UUID> getVoteIds() { return voteIds; }
    public void setVoteIds(Set<UUID> voteIds) { this.voteIds = voteIds; }

    public Set<UUID> getCorrectOptionIds() { return correctOptionIds; }
    public void setCorrectOptionIds(Set<UUID> correctOptionIds) { this.correctOptionIds = correctOptionIds; }
}


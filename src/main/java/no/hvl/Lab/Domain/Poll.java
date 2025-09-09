
// Poll Domain Model: Represents a poll with options, votes, and metadata
package no.hvl.Lab.Domain;

import java.time.Instant;
import java.util.*;

// Poll entity: stores poll question, options, votes, and metadata. Used by backend and frontend for poll management and voting.
 
public class Poll {
    private UUID id;
    private String question;
    private Instant publishedAt;
    private Instant validUntil;
    private boolean publicPoll;             
    private Integer maxVotesPerUser;           
    private Set<String> invitedUsernames = new HashSet<>(); 
    private UUID creatorUserId;
    private List<VoteOption> options = new ArrayList<>();
    private Set<UUID> voteIds = new HashSet<>();

    // Default constructor
    public Poll() {}

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }

    public boolean isPublicPoll() { return publicPoll; }
    public void setPublicPoll(boolean publicPoll) { this.publicPoll = publicPoll; }

    public Integer getMaxVotesPerUser() { return maxVotesPerUser; }
    public void setMaxVotesPerUser(Integer maxVotesPerUser) { this.maxVotesPerUser = maxVotesPerUser; }

    public Set<String> getInvitedUsernames() { return invitedUsernames; }
    public void setInvitedUsernames(Set<String> invitedUsernames) { this.invitedUsernames = invitedUsernames; }

    public UUID getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(UUID creatorUserId) { this.creatorUserId = creatorUserId; }

    public List<VoteOption> getOptions() { return options; }
    public void setOptions(List<VoteOption> options) { this.options = options; }

    public Set<UUID> getVoteIds() { return voteIds; }
    public void setVoteIds(Set<UUID> voteIds) { this.voteIds = voteIds; }
}

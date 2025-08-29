package no.hvl.Lab1.Domain;

import java.time.Instant;
import java.util.UUID;

public class Vote {
    private UUID id;
    private UUID pollId;
    private UUID optionId;

    // null when anonymous vote on public poll
    private UUID voterUserId;

    private boolean anonymous;
    private Instant publishedAt;

    public Vote() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPollId() { return pollId; }
    public void setPollId(UUID pollId) { this.pollId = pollId; }

    public UUID getOptionId() { return optionId; }
    public void setOptionId(UUID optionId) { this.optionId = optionId; }

    public UUID getVoterUserId() { return voterUserId; }
    public void setVoterUserId(UUID voterUserId) { this.voterUserId = voterUserId; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
}

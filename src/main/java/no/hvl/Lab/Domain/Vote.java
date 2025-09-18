
// Vote Domain Model: Represents a user's vote on a poll option 

package no.hvl.Lab.Domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.*;
import java.io.Serializable;

import java.time.Instant;


// Vote entity: stores vote details for a poll option, including user, time, and up/down status.

@Entity
@Table(name = "votes")
public class Vote implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    @JsonIgnore
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    @JsonIgnore
    private VoteOption option;

    @Column(name = "voter_user_id")
    private Long voterUserId;

    private boolean anonymous;
    private Instant publishedAt;
    private boolean isUpvote;
    public Vote() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Poll getPoll() { return poll; }
    public void setPoll(Poll poll) { this.poll = poll; }

    public VoteOption getOption() { return option; }
    public void setOption(VoteOption option) { this.option = option; }

    public Long getVoterUserId() { return voterUserId; }
    public void setVoterUserId(Long voterUserId) { this.voterUserId = voterUserId; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    @JsonProperty("upvote")
    public boolean isUpvote() { return isUpvote; }
    @JsonProperty("upvote")
    public void setUpvote(boolean isUpvote) { this.isUpvote = isUpvote; }

    // Flatten relationships for JSON
    @JsonProperty("pollId")
    public Long getPollId() {
        return poll != null ? poll.getId() : null;
    }
    @JsonProperty("optionId")
    public Long getOptionId() {
        return option != null ? option.getId() : null;
    }
    
}

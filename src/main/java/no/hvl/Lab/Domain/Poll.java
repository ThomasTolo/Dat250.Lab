
// Poll Domain Model: Represents a poll with options, votes, and metadata

package no.hvl.Lab.Domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.*;
import java.io.Serializable;

import java.time.Instant;
import java.util.*;

// Poll entity: stores poll question, options, votes, and metadata. Used by backend and frontend for poll management and voting.
 
@Entity
@Table(name = "polls")
public class Poll implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @Column(nullable = false)
    private String question;
    private Instant publishedAt;
    private Instant validUntil;
    @Column(name = "public_poll")
    private boolean publicPoll;
    private Integer maxVotesPerUser;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "poll_invited_usernames", joinColumns = @JoinColumn(name = "poll_id"))
    @Column(name = "username")
    private Set<String> invitedUsernames = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id")
    @JsonIgnore
    private User createdBy;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<VoteOption> options = new ArrayList<>();

    // Remove voteIds, handled by Vote entity

    /**
     * Adds a new option to this Poll and returns the respective
     * VoteOption object with the given caption.
     * The value of the presentationOrder field gets determined
     * by the size of the currently existing VoteOptions for this Poll.
     * I.e. the first added VoteOption has presentationOrder=0, the secondly
     * registered VoteOption has presentationOrder=1 ans so on.
     */
    public VoteOption addVoteOption(String caption) {
        VoteOption option = new VoteOption();
        option.setCaption(caption);
        option.setPoll(this);
        option.setPresentationOrder(this.options.size());
        this.options.add(option);
        return option;
    }

    // Default constructor
    public Poll() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    // Expose creatorUserId for the frontend without serializing the full User
    @JsonProperty("creatorUserId")
    public Long getCreatorUserId() {
        return createdBy != null ? createdBy.getId() : null;
    }

    public List<VoteOption> getOptions() { return options; }
    public void setOptions(List<VoteOption> options) { this.options = options; }

}

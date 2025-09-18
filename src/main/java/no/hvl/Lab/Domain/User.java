
// User Domain Model: Represents a user in the poll app

package no.hvl.Lab.Domain;

import jakarta.persistence.*;
import java.io.Serializable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.LinkedHashSet;


// User entity: stores user details, created polls, and votes.

@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    private String password;
    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Poll> createdPolls = new HashSet<>();


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Set<Poll> created = new LinkedHashSet<>();
    public User() {}

    /**
     * Creates a new User object with given username and email.
     * The id of a new user object gets determined by the database.
     */
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.created = new LinkedHashSet<>();
    }

    /**
     * Creates a new Poll object for this user
     * with the given poll question
     * and returns it.
     */
    public Poll createPoll(String question) {
        Poll poll = new Poll();
        poll.setQuestion(question);
        poll.setCreatedBy(this);
        this.createdPolls.add(poll);
        return poll;
    }

    /**
     * Creates a new Vote for a given VoteOption in a Poll
     * and returns the Vote as an object.
     */
    public Vote voteFor(VoteOption option) {
        Vote vote = new Vote();
        vote.setOption(option);
        vote.setPoll(option.getPoll());
        vote.setVoterUserId(this.id);
        // Optionally, add to a collection if you want to track votes by user
        return vote;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Set<Poll> getCreatedPolls() { return createdPolls; }
    public void setCreatedPolls(Set<Poll> createdPolls) { this.createdPolls = createdPolls; }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}

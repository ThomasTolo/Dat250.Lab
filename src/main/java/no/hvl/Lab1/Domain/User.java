package no.hvl.Lab1.Domain;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A registered user who can create polls and vote.
 */
public class User {
    private UUID id;
    private String username;
    private String password; // plain for now; hash it later
    private String email;

    // Convenience: track created polls and votes by id
    private Set<UUID> createdPollIds = new HashSet<>();
    private Set<UUID> voteIds = new HashSet<>();

    public User() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Set<UUID> getCreatedPollIds() { return createdPollIds; }
    public void setCreatedPollIds(Set<UUID> createdPollIds) { this.createdPollIds = createdPollIds; }

    public Set<UUID> getVoteIds() { return voteIds; }
    public void setVoteIds(Set<UUID> voteIds) { this.voteIds = voteIds; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}

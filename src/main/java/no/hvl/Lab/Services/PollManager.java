package no.hvl.Lab.Services;

import org.springframework.stereotype.Component;

import no.hvl.Lab.Domain.*;

import java.time.Instant;
import java.util.*;


@Component
public class PollManager {
    public int getNetVotesForOption(UUID pollId, UUID optionId) {
        Poll poll = polls.get(pollId);
        if (poll == null) return 0;
        int net = 0;
        for (UUID vId : poll.getVoteIds()) {
            Vote v = votes.get(vId);
            if (v == null) continue;
            if (optionId.equals(v.getOptionId())) {
                // Upvote = +1, Downvote = -1
                if (v.isUpvote()) {
                    net += 1;
                } else {
                    net -= 1;
                }
            }
        }
        return net;
    }

    private final Map<UUID, User> users = new HashMap<>();
    private final Map<UUID, Poll> polls = new HashMap<>();
    private final Map<UUID, Vote> votes = new HashMap<>();

    // Users
    public User registerUser(String username, String password, String email) {
        // Prevent duplicate usernames
        for (User u : users.values()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                throw new IllegalArgumentException("Username already exists");
            }
        }
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername(username);
        u.setPassword(password);
        u.setEmail(email);
        users.put(u.getId(), u);
        return u;
    }

    public Optional<User> loginUser(String username, String password) {
        for (User u : users.values()) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findUser(UUID id) { return Optional.ofNullable(users.get(id)); }
    public Collection<User> allUsers() { return users.values(); }

    // Polls 
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

    // Votes
    public Vote castVote(UUID pollId, UUID optionId, UUID voterUserId, boolean anonymous) {
        return castVote(pollId, optionId, voterUserId, anonymous, true); // default to upvote for legacy calls
    }

    public Vote castVote(UUID pollId, UUID optionId, UUID voterUserId, boolean anonymous, boolean isUpvote) {
        Poll poll = polls.get(pollId);
        if (poll == null) throw new NoSuchElementException("Poll not found: " + pollId);

        Vote v = new Vote();
        v.setId(UUID.randomUUID());
        v.setPollId(pollId);
        v.setOptionId(optionId);
        v.setVoterUserId(voterUserId);
        v.setAnonymous(anonymous);
        v.setPublishedAt(Instant.now());
        v.setUpvote(isUpvote);

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

    public Vote castOrChangeVote(UUID pollId, UUID optionId, UUID voterUserId, boolean anonymous, boolean isUpvote) {
        Poll poll = polls.get(pollId);
        if (poll == null) throw new NoSuchElementException("Poll not found: " + pollId);

        Instant now = Instant.now();
        // Enforce published/deadline timestamps
        if (poll.getPublishedAt() != null && now.isBefore(poll.getPublishedAt())) {
            throw new IllegalStateException("Poll not yet published");
        }
        if (poll.getValidUntil() != null && now.isAfter(poll.getValidUntil())) {
            throw new IllegalStateException("Poll deadline has passed");
        }

        // Private poll: enforce max votes per user
        if (!poll.isPublicPoll() && poll.getMaxVotesPerUser() != null && voterUserId != null) {
            int userVotes = 0;
            for (UUID vId : poll.getVoteIds()) {
                Vote v = votes.get(vId);
                if (v != null && Objects.equals(v.getVoterUserId(), voterUserId)) {
                    userVotes++;
                }
            }
            if (userVotes >= poll.getMaxVotesPerUser()) {
                throw new IllegalStateException("Max votes per user reached");
            }
        }

        // Public poll: allow anonymous/multiple votes
        // (no restriction, except time window)

        // Find and update existing vote for this user and option
        Vote updatedVote = null;
        for (UUID vId : poll.getVoteIds()) {
            Vote v = votes.get(vId);
            if (v == null) continue;
            if (Objects.equals(v.getOptionId(), optionId) && Objects.equals(v.getVoterUserId(), voterUserId)) {
                v.setUpvote(isUpvote);
                v.setAnonymous(anonymous);
                v.setPublishedAt(now);
                updatedVote = v;
                System.out.println("[DEBUG] Updated vote: id=" + v.getId() + ", isUpvote=" + v.isUpvote());
                break;
            }
        }
        if (updatedVote == null) {
            updatedVote = castVote(pollId, optionId, voterUserId, anonymous, isUpvote);
            System.out.println("[DEBUG] Created vote: id=" + updatedVote.getId() + ", isUpvote=" + updatedVote.isUpvote());
        }
        // Debug: print all votes for this poll after update
        System.out.println("[DEBUG] All votes for poll " + pollId + ":");
        for (UUID vId : poll.getVoteIds()) {
            Vote v = votes.get(vId);
            if (v != null) {
                System.out.println("  voteId=" + v.getId() + ", optionId=" + v.getOptionId() + ", voterUserId=" + v.getVoterUserId() + ", isUpvote=" + v.isUpvote());
            }
        }
        return updatedVote;
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

// Return all votes for a poll
public List<Vote> votesForPoll(UUID pollId) {
    Poll p = polls.get(pollId);
    if (p == null) return List.of();
    List<Vote> result = new ArrayList<>();
    for (UUID vId : p.getVoteIds()) {
        Vote v = votes.get(vId);
        if (v != null) result.add(v);
    }
    return result;
}


}
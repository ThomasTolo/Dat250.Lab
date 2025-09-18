
package no.hvl.Lab.Services;
import jakarta.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import no.hvl.Lab.Domain.*;

@Service
@Transactional
public class PollManager {
    @PersistenceContext
       private EntityManager em;

    @Transactional(readOnly = true)
    public int getNetVotesForOption(Long pollId, Long optionId) {
        Poll poll = em.find(Poll.class, pollId);
        if (poll == null) return 0;
        int net = 0;
        for (VoteOption option : poll.getOptions()) {
            if (option.getId().equals(optionId)) {
                String jpql = "SELECT v FROM Vote v WHERE v.poll.id = :pollId AND v.option.id = :optionId";
                List<Vote> votes = em.createQuery(jpql, Vote.class)
                        .setParameter("pollId", pollId)
                        .setParameter("optionId", optionId)
                        .getResultList();
                for (Vote v : votes) {
                    net += v.isUpvote() ? 1 : -1;
                }
            }
        }
        return net;
    }

    // Users
    public User registerUser(String username, String password, String email) {
        String jpql = "SELECT u FROM User u WHERE LOWER(u.username) = :username";
        List<User> existing = em.createQuery(jpql, User.class)
                .setParameter("username", username.toLowerCase())
                .getResultList();
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("Username already exists");
        }
    User u = new User();
    u.setUsername(username);
    u.setPassword(password);
    u.setEmail(email);
    em.persist(u);
    return u;
    }

    @Transactional(readOnly = true)
    public Optional<User> loginUser(String username, String password) {
        String jpql = "SELECT u FROM User u WHERE LOWER(u.username) = :username AND u.password = :password";
        List<User> users = em.createQuery(jpql, User.class)
                .setParameter("username", username.toLowerCase())
                .setParameter("password", password)
                .getResultList();
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Transactional(readOnly = true)
    public Optional<User> findUser(Long id) {
        User u = em.find(User.class, id);
        return Optional.ofNullable(u);
    }
    @Transactional(readOnly = true)
    public List<User> allUsers() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    // Polls
    public Poll createPoll(Long creatorUserId,
                           String question,
                           boolean isPublic,
                           Instant publishedAt,
                           Instant validUntil,
                           Integer maxVotesPerUser,
                           Collection<String> invitedUsernames,
                           Collection<VoteOption> options) {
        User creator = em.find(User.class, creatorUserId);
        if (creator == null) throw new IllegalArgumentException("User not found");
    Poll p = new Poll();
        p.setCreatedBy(creator);
        p.setQuestion(question);
        p.setPublicPoll(isPublic);
        p.setPublishedAt(publishedAt);
        p.setValidUntil(validUntil);
        p.setMaxVotesPerUser(maxVotesPerUser);
        if (invitedUsernames != null) p.getInvitedUsernames().addAll(invitedUsernames);
        if (options != null) {
            for (VoteOption vo : options) {
                vo.setPoll(p);
                p.getOptions().add(vo);
                em.persist(vo);
            }
            p.getOptions().sort(Comparator.comparingInt(VoteOption::getPresentationOrder));
        }
        em.persist(p);
        return p;
    }

    @Transactional(readOnly = true)
    public Optional<Poll> findPoll(Long id) {
        Poll p = em.find(Poll.class, id);
        return Optional.ofNullable(p);
    }
    @Transactional(readOnly = true)
    public List<Poll> allPolls() {
        return em.createQuery("SELECT p FROM Poll p", Poll.class).getResultList();
    }

    // Votes
    public Vote castVote(Long pollId, Long optionId, Long voterUserId, boolean anonymous) {
        return castVote(pollId, optionId, voterUserId, anonymous, true);
    }

    public Vote castVote(Long pollId, Long optionId, Long voterUserId, boolean anonymous, boolean isUpvote) {
        Poll poll = em.find(Poll.class, pollId);
        if (poll == null) throw new NoSuchElementException("Poll not found: " + pollId);
        VoteOption option = em.find(VoteOption.class, optionId);
        if (option == null) throw new NoSuchElementException("Option not found: " + optionId);
        Vote v = new Vote();
        v.setPoll(poll);
        v.setOption(option);
        v.setVoterUserId(voterUserId);
        v.setAnonymous(anonymous);
        v.setPublishedAt(Instant.now());
        v.setUpvote(isUpvote);
        em.persist(v);
        return v;
    }

    @Transactional(readOnly = true)
    public Optional<Vote> findVote(Long id) {
        Vote v = em.find(Vote.class, id);
        return Optional.ofNullable(v);
    }

    // Convenience queries
    @Transactional(readOnly = true)
    public List<Poll> publicPolls() {
        return em.createQuery("SELECT p FROM Poll p WHERE p.publicPoll = true", Poll.class).getResultList();
    }

    @Transactional(readOnly = true)
    public List<Poll> privatePollsVisibleTo(String username) {
    String jpql = "SELECT DISTINCT p FROM Poll p LEFT JOIN p.invitedUsernames u " +
        "WHERE p.publicPoll = false AND (u = :username OR LOWER(p.createdBy.username) = LOWER(:username))";
    return em.createQuery(jpql, Poll.class)
        .setParameter("username", username)
        .getResultList();
    }

    // delete a poll and its votes
    public void deletePoll(Long pollId) {
        Poll p = em.find(Poll.class, pollId);
        if (p == null) return;
        // Remove votes tied to this poll
        String jpql = "SELECT v FROM Vote v WHERE v.poll.id = :pollId";
        List<Vote> votes = em.createQuery(jpql, Vote.class)
                .setParameter("pollId", pollId)
                .getResultList();
        for (Vote v : votes) {
            em.remove(v);
        }
        em.remove(p);
    }

    // change vote = keep only the newest vote per user for listing
    public Vote castOrChangeVote(Long pollId, Long optionId, Long voterUserId, boolean anonymous, boolean isUpvote) {
        Poll poll = em.find(Poll.class, pollId);
        if (poll == null) throw new NoSuchElementException("Poll not found: " + pollId);
        VoteOption option = em.find(VoteOption.class, optionId);
        if (option == null) throw new NoSuchElementException("Option not found: " + optionId);
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
            String jpql = "SELECT COUNT(v) FROM Vote v WHERE v.poll.id = :pollId AND v.voterUserId = :voterUserId";
            Long userVotes = em.createQuery(jpql, Long.class)
                    .setParameter("pollId", pollId)
                    .setParameter("voterUserId", voterUserId)
                    .getSingleResult();
            if (userVotes >= poll.getMaxVotesPerUser()) {
                throw new IllegalStateException("Max votes per user reached");
            }
        }
        // Find and update existing vote for this user and option
        String jpql = "SELECT v FROM Vote v WHERE v.poll.id = :pollId AND v.option.id = :optionId AND v.voterUserId = :voterUserId";
        List<Vote> existingVotes = em.createQuery(jpql, Vote.class)
                .setParameter("pollId", pollId)
                .setParameter("optionId", optionId)
                .setParameter("voterUserId", voterUserId)
                .getResultList();
        Vote updatedVote = null;
        if (!existingVotes.isEmpty()) {
            updatedVote = existingVotes.get(0);
            updatedVote.setUpvote(isUpvote);
            updatedVote.setAnonymous(anonymous);
            updatedVote.setPublishedAt(now);
            em.merge(updatedVote);
        } else {
            updatedVote = castVote(pollId, optionId, voterUserId, anonymous, isUpvote);
        }
        return updatedVote;
    }

    // latest per user (deduplicate by voterUserId, keep most recent)
    @Transactional(readOnly = true)
    public List<Vote> votesForPollLatestPerUser(Long pollId) {
        String jpql = "SELECT v FROM Vote v WHERE v.poll.id = :pollId ORDER BY v.publishedAt DESC";
        List<Vote> allVotes = em.createQuery(jpql, Vote.class)
                .setParameter("pollId", pollId)
                .getResultList();
        Map<Long, Vote> latest = new HashMap<>();
        for (Vote v : allVotes) {
            Long key = v.getVoterUserId();
            if (key != null && !latest.containsKey(key)) {
                latest.put(key, v);
            }
        }
        return new ArrayList<>(latest.values());
    }

    // Return all votes for a poll
    @Transactional(readOnly = true)
    public List<Vote> votesForPoll(Long pollId) {
        String jpql = "SELECT v FROM Vote v WHERE v.poll.id = :pollId";
        return em.createQuery(jpql, Vote.class)
                .setParameter("pollId", pollId)
                .getResultList();
    }

}
package no.hvl.Lab.Services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import no.hvl.Lab.Domain.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;



@Service
@Transactional
public class PollManager2 {

    @PersistenceContext
    private EntityManager em;

    /* ---------------- Users ---------------- */
    public User registerUser(String username, String password, String email) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username required");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required");
        String jpql = "SELECT u FROM User u WHERE LOWER(u.username) = :u";
        boolean exists = !em.createQuery(jpql, User.class)
                .setParameter("u", username.toLowerCase())
                .getResultList().isEmpty();
        if (exists) throw new IllegalArgumentException("Username already exists");
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setEmail(email);
        em.persist(u);
        return u;
    }

    public Optional<User> loginUser(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        String jpql = "SELECT u FROM User u WHERE LOWER(u.username)=:u AND u.password=:p";
        List<User> list = em.createQuery(jpql, User.class)
                .setParameter("u", username.toLowerCase())
                .setParameter("p", password)
                .getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<User> findUser(Long id) { return Optional.ofNullable(em.find(User.class, id)); }
    public List<User> allUsers() { return em.createQuery("SELECT u FROM User u", User.class).getResultList(); }

    /* ---------------- Polls ---------------- */
    public Poll createPoll(Long creatorUserId,
                           String question,
                           boolean isPublic,
                           Instant publishedAt,
                           Instant validUntil,
                           Integer maxVotesPerUser,
                           Collection<String> invitedUsernames,
                           Collection<VoteOption> initialOptions) {
        User creator = em.find(User.class, creatorUserId);
        if (creator == null) throw new IllegalArgumentException("User not found");
        if (question == null || question.isBlank()) throw new IllegalArgumentException("Question required");
        // Use domain helper (mirrors assignment pattern)
        Poll poll = creator.createPoll(question);
        poll.setPublicPoll(isPublic);
        poll.setPublishedAt(publishedAt);
        poll.setValidUntil(validUntil);
        poll.setMaxVotesPerUser(maxVotesPerUser);
        if (invitedUsernames != null) poll.getInvitedUsernames().addAll(invitedUsernames);
        if (initialOptions != null) {
            initialOptions.stream()
                    .sorted(Comparator.comparingInt(VoteOption::getPresentationOrder))
                    .forEach(vo -> poll.addVoteOption(vo.getCaption()));
        }
        em.persist(poll);
        return poll;
    }

    public Optional<Poll> findPoll(Long id) { return Optional.ofNullable(em.find(Poll.class, id)); }
    public List<Poll> allPolls() { return em.createQuery("SELECT p FROM Poll p", Poll.class).getResultList(); }
    public List<Poll> publicPolls() { return em.createQuery("SELECT p FROM Poll p WHERE p.publicPoll = true", Poll.class).getResultList(); }

    public List<Poll> privatePollsVisibleTo(String username) {
        if (username == null) return List.of();
        String jpql = "SELECT DISTINCT p FROM Poll p LEFT JOIN p.invitedUsernames u " +
                "WHERE p.publicPoll = false AND (u = :username OR LOWER(p.createdBy.username)=LOWER(:username))";
        return em.createQuery(jpql, Poll.class)
                .setParameter("username", username)
                .getResultList();
    }

    @CacheEvict(value = "poll-vote-counts", key = "#pollId")
    public void deletePoll(Long pollId) {
        Poll p = em.find(Poll.class, pollId);
        if (p == null) return;
        em.remove(p);
    }

    /* ---------------- Votes ---------------- */
    public Vote castVote(Long pollId, Long optionId, Long voterUserId, boolean anonymous) {
        return castVote(pollId, optionId, voterUserId, anonymous, true);
    }

    @CacheEvict(value = "poll-vote-counts", key = "#pollId")
    public Vote castVote(Long pollId, Long optionId, Long voterUserId, boolean anonymous, boolean isUpvote) {
        Poll poll = em.find(Poll.class, pollId);
        if (poll == null) throw new NoSuchElementException("Poll not found: " + pollId);
        VoteOption option = em.find(VoteOption.class, optionId);
        if (option == null) throw new NoSuchElementException("Option not found: " + optionId);
        if (!Objects.equals(option.getPoll().getId(), pollId)) throw new IllegalArgumentException("Option does not belong to poll");
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

    @CacheEvict(value = "poll-vote-counts", key = "#pollId")
    public Vote castOrChangeVote(Long pollId, Long optionId, Long voterUserId, boolean anonymous, boolean isUpvote) {
        Poll poll = em.find(Poll.class, pollId);
        if (poll == null) throw new NoSuchElementException("Poll not found: " + pollId);
        VoteOption option = em.find(VoteOption.class, optionId);
        if (option == null) throw new NoSuchElementException("Option not found: " + optionId);
        Instant now = Instant.now();
        if (poll.getPublishedAt() != null && now.isBefore(poll.getPublishedAt())) throw new IllegalStateException("Poll not yet published");
        if (poll.getValidUntil() != null && now.isAfter(poll.getValidUntil())) throw new IllegalStateException("Poll deadline has passed");
        if (!poll.isPublicPoll() && poll.getMaxVotesPerUser() != null && voterUserId != null) {
            String countJpql = "SELECT COUNT(v) FROM Vote v WHERE v.poll.id = :pid AND v.voterUserId = :uid";
            Long userVotes = em.createQuery(countJpql, Long.class)
                    .setParameter("pid", pollId)
                    .setParameter("uid", voterUserId)
                    .getSingleResult();
            if (userVotes >= poll.getMaxVotesPerUser()) throw new IllegalStateException("Max votes per user reached");
        }
        String findExisting = "SELECT v FROM Vote v WHERE v.poll.id = :pid AND v.option.id = :oid AND v.voterUserId = :uid";
        List<Vote> existing = em.createQuery(findExisting, Vote.class)
                .setParameter("pid", pollId)
                .setParameter("oid", optionId)
                .setParameter("uid", voterUserId)
                .getResultList();
        if (!existing.isEmpty()) {
            Vote v = existing.get(0);
            v.setUpvote(isUpvote);
            v.setAnonymous(anonymous);
            v.setPublishedAt(now);
            return v;
        }
        return castVote(pollId, optionId, voterUserId, anonymous, isUpvote);
    }

    public Optional<Vote> findVote(Long id) { return Optional.ofNullable(em.find(Vote.class, id)); }

    public List<Vote> votesForPoll(Long pollId) {
        String jpql = "SELECT v FROM Vote v WHERE v.poll.id = :pid";
        return em.createQuery(jpql, Vote.class).setParameter("pid", pollId).getResultList();
    }

    public List<Vote> votesForPollLatestPerUser(Long pollId) {
        String jpql = "SELECT v FROM Vote v WHERE v.poll.id = :pid ORDER BY v.publishedAt DESC";
        List<Vote> all = em.createQuery(jpql, Vote.class).setParameter("pid", pollId).getResultList();
        Map<Long, Vote> latest = new LinkedHashMap<>();
        for (Vote v : all) {
            Long key = v.getVoterUserId();
            if (key != null && !latest.containsKey(key)) latest.put(key, v);
        }
        return new ArrayList<>(latest.values());
    }

    public int getNetVotesForOption(Long pollId, Long optionId) {
        String jpql = "SELECT v FROM Vote v WHERE v.poll.id = :pid AND v.option.id = :oid";
        List<Vote> list = em.createQuery(jpql, Vote.class)
                .setParameter("pid", pollId)
                .setParameter("oid", optionId)
                .getResultList();
        int net = 0;
        for (Vote v : list) net += v.isUpvote() ? 1 : -1;
        return net;
    }

    @Cacheable(value = "poll-vote-counts", key = "#pollId")
    public Map<Long, Integer> getVoteCountsForPoll(Long pollId) {
        String jpql = "SELECT v.option.id, COUNT(v.id) FROM Vote v WHERE v.poll.id = :pid GROUP BY v.option.id";
        List<Object[]> rows = em.createQuery(jpql, Object[].class)
                .setParameter("pid", pollId)
                .getResultList();
        Map<Long, Integer> map = new HashMap<>();
        for (Object[] r : rows) {
            Long oid = (Long) r[0];
            Long cnt = (Long) r[1];
            map.put(oid, cnt.intValue());
        }
        return map;
    }
}

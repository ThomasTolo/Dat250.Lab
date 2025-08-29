package no.hvl.Lab1.Web;

import no.hvl.Lab1.Domain.Poll;
import no.hvl.Lab1.Domain.VoteOption;
import no.hvl.Lab1.Service.PollManager;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/polls")
public class PollController {
    private final PollManager manager;
    public PollController(PollManager manager) { this.manager = manager; }

    @PostMapping
    public Poll create(@RequestBody CreatePollRequest req) {
        return manager.createPoll(
                req.creatorUserId,
                req.question,
                req.publicPoll,
                req.publishedAt,
                req.validUntil,
                req.maxVotesPerUser,
                req.invitedUsernames,
                req.options
        );
    }

    @GetMapping
    public Collection<Poll> list() { return manager.allPolls(); }

    @DeleteMapping("/{pollId}")
    public void delete(@PathVariable UUID pollId) { manager.deletePoll(pollId); }

    public static class CreatePollRequest {
        public UUID creatorUserId;
        public String question;
        public boolean publicPoll;
        public Instant publishedAt;
        public Instant validUntil;
        public Integer maxVotesPerUser;              // null => unlimited
        public Set<String> invitedUsernames;         // for private polls
        public List<VoteOption> options;             // caption + presentationOrder
    }
}

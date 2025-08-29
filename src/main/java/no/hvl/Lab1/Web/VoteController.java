package no.hvl.Lab1.Web;

import no.hvl.Lab1.Domain.Vote;
import no.hvl.Lab1.Service.PollManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/polls/{pollId}/votes")
public class VoteController {
    private final PollManager manager;
    public VoteController(PollManager manager) { this.manager = manager; }

    @PostMapping
    public Vote cast(@PathVariable UUID pollId, @RequestBody VoteRequest req) {
        return manager.castOrChangeVote(pollId, req.optionId, req.voterUserId, req.anonymous);
    }

    // returns latest vote per user for this poll
    @GetMapping
    public List<Vote> listLatestPerUser(@PathVariable UUID pollId) {
        return manager.votesForPollLatestPerUser(pollId);
    }

    public static class VoteRequest {
        public UUID optionId;
        public UUID voterUserId; // null if anonymous
        public boolean anonymous;
    }
}

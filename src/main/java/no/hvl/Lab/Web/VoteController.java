
package no.hvl.Lab.Web;

import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.*;

import no.hvl.Lab.Domain.Vote;
import no.hvl.Lab.Service.PollManager;

import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api/polls/{pollId}/votes")
public class VoteController {
    private final PollManager manager;
    public VoteController(PollManager manager) { this.manager = manager; }

    @PostMapping
    public Vote cast(@PathVariable UUID pollId, @RequestBody VoteRequest req) {
    // Pass isUpvote to manager and always return the updated vote object
    Vote vote = manager.castOrChangeVote(pollId, req.optionId, req.voterUserId, req.anonymous, req.isUpvote);
    // Re-fetch from manager to ensure latest state
    return manager.findVote(vote.getId()).orElse(vote);
    }

    @GetMapping
    public List<Vote> listLatestPerUser(@PathVariable UUID pollId) {
        return manager.votesForPollLatestPerUser(pollId);
    }

    @GetMapping("/{voteId}")
    public Vote get(@PathVariable UUID pollId, @PathVariable UUID voteId) {
        return manager.findVote(voteId).orElseThrow();
    }

    @PutMapping("/{voteId}")
    public Vote update(@PathVariable UUID pollId, @PathVariable UUID voteId, @RequestBody VoteRequest req) {
    // For demo: delete and re-cast
    // (real app: update fields)
    // Not implemented: deleteVote
    // Pass isUpvote to manager and always return the updated vote object
    Vote vote = manager.castOrChangeVote(pollId, req.optionId, req.voterUserId, req.anonymous, req.isUpvote);
    return manager.findVote(vote.getId()).orElse(vote);
    }

    @DeleteMapping("/{voteId}")
    public void delete(@PathVariable UUID pollId, @PathVariable UUID voteId) {
        // Not implemented: deleteVote
    }

    public static class VoteRequest {
    public UUID optionId;
    public UUID voterUserId; // null if anonymous
    public boolean anonymous;
    public boolean isUpvote;
    }
}

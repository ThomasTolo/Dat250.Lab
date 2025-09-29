package no.hvl.Lab.Controllers;

import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.*;

import no.hvl.Lab.Domain.Vote;
import no.hvl.Lab.Services.PollManager;
import no.hvl.Lab.RawWebSocketServer;

import java.util.List;


@CrossOrigin
@RestController
@RequestMapping("/api/polls/{pollId}/votes")
public class VoteController {
    private final PollManager manager;
    public VoteController(PollManager manager) { this.manager = manager; }

    @PostMapping
    public Vote cast(@PathVariable Long pollId, @RequestBody VoteRequest req) {
        Vote vote = manager.castOrChangeVote(pollId, req.optionId, req.voterUserId, req.anonymous, req.isUpvote);
        RawWebSocketServer.broadcast("votesUpdated");
        return manager.findVote(vote.getId()).orElse(vote);
    }

    @GetMapping
    public List<Vote> listAllVotes(@PathVariable Long pollId) {
        return manager.votesForPoll(pollId);
    }

    @GetMapping("/{voteId}")
    public Vote get(@PathVariable Long pollId, @PathVariable Long voteId) {
        return manager.findVote(voteId).orElseThrow();
    }

    @PutMapping("/{voteId}")
    public Vote update(@PathVariable Long pollId, @PathVariable Long voteId, @RequestBody VoteRequest req) {
        Vote vote = manager.castOrChangeVote(pollId, req.optionId, req.voterUserId, req.anonymous, req.isUpvote);
        return manager.findVote(vote.getId()).orElse(vote);
    }

    @DeleteMapping("/{voteId}")
    public void delete(@PathVariable Long pollId, @PathVariable Long voteId) {
    }

    public static class VoteRequest {
    public Long optionId;
        public Long voterUserId; 
        public boolean anonymous;
        public boolean isUpvote;
    }
}

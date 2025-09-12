package no.hvl.Lab.Controllers;

import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.*;

import no.hvl.Lab.Domain.Poll;
import no.hvl.Lab.Domain.VoteOption;
import no.hvl.Lab.Services.PollManager;
import no.hvl.Lab.RawWebSocketServer;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api/polls")
public class PollController {
    private final PollManager manager;
    public PollController(PollManager manager) { this.manager = manager; }

    @PostMapping
    public Poll create(@RequestBody CreatePollRequest req) {
        Poll poll = manager.createPoll(
                req.creatorUserId,
                req.question,
                req.publicPoll,
                req.publishedAt,
                req.validUntil,
                req.maxVotesPerUser,
                req.invitedUsernames,
                req.options
        );
        RawWebSocketServer.broadcast("pollsUpdated");
        return poll;
    }

    @GetMapping
    public Collection<Poll> list() { return manager.allPolls(); }

    @GetMapping("/{pollId}")
    public Poll get(@PathVariable UUID pollId) {
        return manager.findPoll(pollId).orElseThrow();
    }

    @PutMapping("/{pollId}")
    public Poll update(@PathVariable UUID pollId, @RequestBody CreatePollRequest req) {
        manager.deletePoll(pollId);
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

    @DeleteMapping("/{pollId}")
    public void delete(@PathVariable UUID pollId) { manager.deletePoll(pollId); }

    public static class CreatePollRequest {
        public UUID creatorUserId;
        public String question;
        public boolean publicPoll;
        public Instant publishedAt;
        public Instant validUntil;
        public Integer maxVotesPerUser;              
        public Set<String> invitedUsernames;         
        public List<VoteOption> options;             
    }
}
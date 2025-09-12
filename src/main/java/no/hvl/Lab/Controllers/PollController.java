package no.hvl.Lab.Controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import no.hvl.Lab.RawWebSocketServer;
import no.hvl.Lab.Domain.Poll;
import no.hvl.Lab.Domain.VoteOption;
import no.hvl.Lab.Services.PollManager;

import java.util.*;
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

	@DeleteMapping("/{pollId}")
	public void delete(@PathVariable UUID pollId) {
		manager.deletePoll(pollId);
		RawWebSocketServer.broadcast("pollsUpdated");
	}

	public static class CreatePollRequest {
		public UUID creatorUserId;
		public String question;
		public boolean publicPoll;
		public java.time.Instant publishedAt;
		public java.time.Instant validUntil;
		public Integer maxVotesPerUser;
		public Collection<String> invitedUsernames;
		public Collection<VoteOption> options;
	}
}
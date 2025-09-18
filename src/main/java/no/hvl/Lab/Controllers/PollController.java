package no.hvl.Lab.Controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import no.hvl.Lab.RawWebSocketServer;
import no.hvl.Lab.Domain.Poll;
import no.hvl.Lab.Domain.VoteOption;
import no.hvl.Lab.Services.PollManager;

import java.util.*;


@CrossOrigin
@RestController
@RequestMapping("/api/polls")
public class PollController {
	private final PollManager manager;
	public PollController(PollManager manager) { this.manager = manager; }

	@PostMapping
	public Poll create(@RequestBody CreatePollRequest req, @RequestParam(required = true) Long userId) {
		// Only allow poll creation if userId matches a registered user
		if (!manager.findUser(userId).isPresent()) {
			throw new IllegalArgumentException("User must be registered to create a poll");
		}
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
	public Collection<Poll> list(@RequestParam(required = false) String username) {
		if (username == null || username.isEmpty()) {
			return manager.publicPolls();
		} else {
			List<Poll> result = new ArrayList<>();
			result.addAll(manager.publicPolls());
			result.addAll(manager.privatePollsVisibleTo(username));
			return result;
		}
	}

	@GetMapping("/{pollId}")
	public Poll get(@PathVariable Long pollId) {
		return manager.findPoll(pollId).orElseThrow();
	}

	@DeleteMapping("/{pollId}")
	public void delete(@PathVariable Long pollId, @RequestParam(required = true) Long userId) {
		// Only allow the creator to delete
		Poll p = manager.findPoll(pollId).orElseThrow();
		Long creatorId = p.getCreatedBy() != null ? p.getCreatedBy().getId() : null;
		if (creatorId == null || !creatorId.equals(userId)) {
			throw new IllegalArgumentException("Only the creator can delete this poll");
		}
		manager.deletePoll(pollId);
		RawWebSocketServer.broadcast("pollsUpdated");
	}

	public static class CreatePollRequest {
	public Long creatorUserId;
		public String question;
		public boolean publicPoll;
		public java.time.Instant publishedAt;
		public java.time.Instant validUntil;
		public Integer maxVotesPerUser;
		public Collection<String> invitedUsernames;
		public Collection<VoteOption> options;
	}
}
package no.hvl.Lab.Services;

import no.hvl.Lab.Config.RabbitConfig;
import no.hvl.Lab.RawWebSocketServer;
import no.hvl.Lab.Domain.Vote;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class PollEventMessagingService {
    private static final Logger log = LoggerFactory.getLogger(PollEventMessagingService.class);
    private final RabbitTemplate rabbitTemplate;
    private final PollManager pollManager;
    private final RabbitAdmin rabbitAdmin;
    private final ObjectMapper mapper;

    public PollEventMessagingService(RabbitTemplate rabbitTemplate, PollManager pollManager, RabbitAdmin rabbitAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.pollManager = pollManager;
        this.rabbitAdmin = rabbitAdmin;
    this.mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void publishPollCreated(Long pollId) {
        try {
            // Ensure a dedicated per-poll queue exists (optional; external clients can bind/consume it).
            ensurePerPollQueue(pollId);
            PollCreated evt = new PollCreated();
            evt.type = "PollCreated"; evt.pollId = pollId; evt.source = "app"; // mark as internally originated
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, routingKeyFor(pollId), mapper.writeValueAsString(evt));
        } catch (Exception e) { log.warn("Failed to publish PollCreated", e); }
    }

    public void publishVoteEvent(Long pollId, Long optionId, Long voterUserId, boolean anonymous, boolean upvote) {
        try {
            VoteEvent evt = new VoteEvent();
            evt.type = "Vote"; evt.pollId = pollId; evt.optionId = optionId; evt.upvote = upvote; evt.anonymous = anonymous; evt.voterUserId = voterUserId; evt.source = "app"; // internal marker
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, routingKeyFor(pollId), mapper.writeValueAsString(evt));
        } catch (Exception e) { log.warn("Failed to publish Vote event", e); }
    }

    private String routingKeyFor(Long pollId) { return "poll." + pollId; }

    private String perPollQueueName(Long pollId) { return "poll-" + pollId; }

    
    public void ensurePerPollQueue(Long pollId) {
        try {
            String qName = perPollQueueName(pollId);
            Queue q = new Queue(qName, true, false, false);
            rabbitAdmin.declareQueue(q);
            Binding b = new Binding(qName, DestinationType.QUEUE, RabbitConfig.EXCHANGE_NAME, routingKeyFor(pollId), null);
            rabbitAdmin.declareBinding(b);
        } catch (Exception e) {
            log.warn("Failed to declare per-poll queue for poll {}", pollId, e);
        }
    }

    @RabbitListener(queues = RabbitConfig.EVENTS_QUEUE)
    public void onPollEvent(String body) {
        try {
            BaseEvent base = mapper.readValue(body, BaseEvent.class);
            if (base == null || base.type == null) return;
            String t = base.type;
            if (t != null) t = t.trim();
            if (t == null) return;
            switch (t) {
                case "PollCreated", "pollCreated" -> {
                }
                case "Vote", "vote" -> {
                    VoteEvent v = mapper.readValue(body, VoteEvent.class);
                    if (v.pollId == null || v.optionId == null || v.optionId <= 0) return; 
                    Vote persisted = pollManager.castOrChangeVote(v.pollId, v.optionId, v.voterUserId, v.anonymous != null && v.anonymous, v.upvote != null && v.upvote);
                    // Only broadcast to WebSocket clients if NOT internally originated 
                    if (v.source == null || !"app".equalsIgnoreCase(v.source)) {
                        String payload = String.format(java.util.Locale.ROOT,
                                "{\"type\":\"vote-delta\",\"pollId\":%d,\"optionId\":%d,\"voteId\":%d,\"upvote\":%s,\"voterUserId\":%s,\"ts\":%d}",
                                persisted.getPollId(),
                                persisted.getOptionId(),
                                persisted.getId(),
                                persisted.isUpvote(),
                                persisted.getVoterUserId() == null ? "null" : persisted.getVoterUserId().toString(),
                                System.currentTimeMillis());
                        RawWebSocketServer.broadcast(payload);
                    }
                }
                default -> { }
            }
        } catch (Exception e) {
            log.warn("Failed to process poll event: {}", body, e);
        }
    }

    static class BaseEvent { public String type; }
    static class PollCreated extends BaseEvent { public Long pollId; public String source; }
    static class VoteEvent extends BaseEvent { public Long pollId; public Long optionId; public Boolean upvote; public Boolean anonymous; public Long voterUserId; public String source; }
}

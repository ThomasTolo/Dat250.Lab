package no.hvl.Lab.Services;

import no.hvl.Lab.Config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Very small, read‑only listener that just logs every raw event body arriving on the
 * shared poll events queue. This is purely demonstrational so you can SEE that the
 * listener mechanism works, without touching business logic in {@link PollEventMessagingService}.
 *
 * You can safely delete this class later, or expand it to add metrics / auditing.
 */
@Component
public class PollEventLoggerListener {
    private static final Logger log = LoggerFactory.getLogger(PollEventLoggerListener.class);

    @RabbitListener(queues = RabbitConfig.EVENTS_QUEUE)
    public void logIncomingEvent(String body) {
        // Keep it minimal: just log the message. Avoid expensive parsing here.
        log.info("[poll-events][raw] {}", body);
    }
}

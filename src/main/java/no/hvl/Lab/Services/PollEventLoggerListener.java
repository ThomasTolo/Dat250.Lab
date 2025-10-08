package no.hvl.Lab.Services;

import no.hvl.Lab.Config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
public class PollEventLoggerListener {
    private static final Logger log = LoggerFactory.getLogger(PollEventLoggerListener.class);

    @RabbitListener(queues = RabbitConfig.EVENTS_QUEUE)
    public void logIncomingEvent(String body) {
        log.info("[poll-events][raw] {}", body);
    }
}

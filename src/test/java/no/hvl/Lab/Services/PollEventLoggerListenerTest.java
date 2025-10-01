package no.hvl.Lab.Services;

import org.junit.jupiter.api.Test;

/**
 * Simple unit test for {@link PollEventLoggerListener}. Since the listener only logs
 * the raw message body, this test just calls the method to ensure no exceptions occur.
 *
 * If later you add parsing/validation logic here, expand tests accordingly.
 */
public class PollEventLoggerListenerTest {

    @Test
    void logsRawMessageWithoutError() {
        PollEventLoggerListener listener = new PollEventLoggerListener();
        listener.logIncomingEvent("{\"type\":\"Vote\",\"pollId\":1,\"optionId\":2,\"upvote\":true}");
    }
}

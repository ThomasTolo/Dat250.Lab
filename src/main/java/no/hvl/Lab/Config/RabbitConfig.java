package no.hvl.Lab.Config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Basic RabbitMQ infrastructure: a topic exchange to which per-poll routing keys
 * (poll.{pollId}) are published for vote events, and a durable queue the application
 * consumes from (poll-events). Using a single queue keeps consumption logic simple;
 * we rely on routing keys to distinguish polls. If you later want true fan‑out to
 * multiple independent consumers, you can create a queue per poll instead.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "poll-exchange";
    public static final String EVENTS_QUEUE = "poll-events"; // single sink for all poll events

    @Bean
    public TopicExchange pollExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
    }

    @Bean
    public Queue pollEventsQueue() {
        return QueueBuilder.durable(EVENTS_QUEUE).build();
    }

    @Bean
    public Binding pollEventsBinding(TopicExchange pollExchange, Queue pollEventsQueue) {
        // Listen to all poll.* routing keys
        return BindingBuilder.bind(pollEventsQueue).to(pollExchange).with("poll.*");
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        return tpl;
    }
}
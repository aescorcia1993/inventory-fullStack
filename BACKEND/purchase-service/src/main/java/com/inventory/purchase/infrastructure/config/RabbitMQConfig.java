package com.inventory.purchase.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "inventory.exchange";
    public static final String QUEUE = "purchase.completed.queue";
    public static final String ROUTING_KEY = "purchase.completed";

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue purchaseCompletedQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding purchaseCompletedBinding(Queue purchaseCompletedQueue, TopicExchange inventoryExchange) {
        return BindingBuilder.bind(purchaseCompletedQueue).to(inventoryExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                          Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}

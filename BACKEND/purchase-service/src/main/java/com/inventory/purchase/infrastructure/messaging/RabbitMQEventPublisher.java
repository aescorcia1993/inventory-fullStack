package com.inventory.purchase.infrastructure.messaging;

import com.inventory.purchase.domain.model.Purchase;
import com.inventory.purchase.domain.port.out.EventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RabbitMQEventPublisher implements EventPublisher {

    static final String EXCHANGE = "inventory.exchange";
    static final String ROUTING_KEY = "purchase.completed";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishPurchaseCompleted(Purchase purchase) {
        PurchaseCompletedEvent event = new PurchaseCompletedEvent(
                purchase.getId(),
                purchase.getProductoId(),
                purchase.getCantidad(),
                purchase.getTotal(),
                Instant.now());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
}

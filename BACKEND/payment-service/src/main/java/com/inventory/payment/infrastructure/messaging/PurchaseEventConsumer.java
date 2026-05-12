package com.inventory.payment.infrastructure.messaging;

import com.inventory.payment.infrastructure.config.RabbitMQConfig;
import com.inventory.payment.infrastructure.persistence.PaymentEntity;
import com.inventory.payment.infrastructure.persistence.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PurchaseEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PurchaseEventConsumer.class);

    private final PaymentRepository repository;

    public PurchaseEventConsumer(PaymentRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handle(PurchaseCompletedEvent event) throws InterruptedException {
        log.info("⚙️  Processing payment for purchase {} — producto {} × {}",
                event.purchaseId(), event.productoId(), event.cantidad());

        PaymentEntity entity = new PaymentEntity();
        entity.setId(UUID.randomUUID());
        entity.setPurchaseId(event.purchaseId());
        entity.setProductoId(event.productoId());
        entity.setCantidad(event.cantidad());
        entity.setTotal(event.total());
        entity.setStatus("PROCESSING");
        entity.setReceivedAt(Instant.now());
        repository.save(entity);

        // Simulated payment gateway delay
        Thread.sleep(1_000);

        entity.setStatus("COMPLETED");
        entity.setProcessedAt(Instant.now());
        repository.save(entity);

        log.info("✅ Payment completed for purchase {} — total {}",
                event.purchaseId(), event.total());
    }
}

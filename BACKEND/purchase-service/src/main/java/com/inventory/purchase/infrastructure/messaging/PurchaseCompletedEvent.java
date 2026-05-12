package com.inventory.purchase.infrastructure.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PurchaseCompletedEvent(
        UUID purchaseId,
        UUID productoId,
        int cantidad,
        BigDecimal total,
        Instant occurredAt) {}

package com.inventory.payment.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentAttributes(
        UUID purchaseId,
        UUID productoId,
        int cantidad,
        BigDecimal total,
        String status,
        Instant receivedAt,
        Instant processedAt) {}

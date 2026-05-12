package com.inventory.purchase.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PurchaseAttributes(UUID productoId, int cantidad, BigDecimal precioUnitario,
                                  BigDecimal total, Instant createdAt) {}

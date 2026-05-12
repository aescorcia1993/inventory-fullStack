package com.inventory.inventory.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record InventoryAttributes(UUID productoId, int cantidad, Instant updatedAt) {}

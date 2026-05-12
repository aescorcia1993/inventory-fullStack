package com.inventory.inventory.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateInventoryRequest(@NotNull @Valid CreateInventoryData data) {
    public record CreateInventoryData(String type, @NotNull @Valid CreateInventoryAttributes attributes) {}
    public record CreateInventoryAttributes(
            @NotNull java.util.UUID productoId,
            @jakarta.validation.constraints.PositiveOrZero int cantidad) {}
}

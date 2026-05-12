package com.inventory.inventory.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateInventoryRequest(@NotNull @Valid UpdateInventoryData data) {
    public record UpdateInventoryData(String type, @NotNull @Valid UpdateInventoryAttributes attributes) {}
    public record UpdateInventoryAttributes(@PositiveOrZero int cantidad) {}
}

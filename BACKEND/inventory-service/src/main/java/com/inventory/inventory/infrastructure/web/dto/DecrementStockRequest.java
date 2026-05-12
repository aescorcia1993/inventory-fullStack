package com.inventory.inventory.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DecrementStockRequest(@NotNull @Valid DecrementStockData data) {
    public record DecrementStockData(String type, @NotNull @Valid DecrementStockAttributes attributes) {}
    public record DecrementStockAttributes(@Positive int amount) {}
}

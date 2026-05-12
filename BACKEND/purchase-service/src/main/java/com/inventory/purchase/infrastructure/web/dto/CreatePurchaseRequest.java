package com.inventory.purchase.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreatePurchaseRequest(@NotNull @Valid CreatePurchaseData data) {
    public record CreatePurchaseData(String type, @NotNull @Valid CreatePurchaseAttributes attributes) {}
    public record CreatePurchaseAttributes(
            @NotNull UUID productoId,
            @Positive int cantidad) {}
}

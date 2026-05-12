package com.inventory.products.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateProductRequest(CreateProductData data) {

    public record CreateProductData(String type, CreateProductAttributes attributes) {}

    public record CreateProductAttributes(
            @NotBlank(message = "nombre es requerido") String nombre,
            @NotNull(message = "precio es requerido") @Positive(message = "precio debe ser mayor a 0") BigDecimal precio,
            String descripcion
    ) {}
}

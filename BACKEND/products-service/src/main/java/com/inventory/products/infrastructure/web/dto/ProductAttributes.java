package com.inventory.products.infrastructure.web.dto;

import java.math.BigDecimal;

public record ProductAttributes(String nombre, BigDecimal precio, String descripcion) {}

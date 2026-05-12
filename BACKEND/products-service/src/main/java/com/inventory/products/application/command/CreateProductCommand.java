package com.inventory.products.application.command;

import java.math.BigDecimal;

public record CreateProductCommand(String nombre, BigDecimal precio, String descripcion) {}

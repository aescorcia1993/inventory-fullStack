package com.inventory.products.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Product {

    private final UUID id;
    private final String nombre;
    private final BigDecimal precio;
    private final String descripcion;

    public Product(UUID id, String nombre, BigDecimal precio, String descripcion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre es requerido");
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("precio debe ser mayor a 0");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.nombre = nombre.trim();
        this.precio = precio;
        this.descripcion = descripcion;
    }

    public UUID getId()          { return id; }
    public String getNombre()    { return nombre; }
    public BigDecimal getPrecio(){ return precio; }
    public String getDescripcion(){ return descripcion; }
}

package com.inventory.inventory.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Inventory {

    private final UUID id;
    private final UUID productoId;
    private int cantidad;
    private Instant updatedAt;

    public Inventory(UUID id, UUID productoId, int cantidad) {
        if (productoId == null) throw new IllegalArgumentException("productoId es requerido");
        if (cantidad < 0) throw new IllegalArgumentException("cantidad no puede ser negativa");
        this.id = id != null ? id : UUID.randomUUID();
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.updatedAt = Instant.now();
    }

    public void decrement(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("El monto a descontar debe ser positivo");
        if (this.cantidad < amount) {
            throw new InsufficientStockException(
                    "Stock insuficiente. Disponible: " + this.cantidad + ", solicitado: " + amount);
        }
        this.cantidad -= amount;
        this.updatedAt = Instant.now();
    }

    public void updateCantidad(int newCantidad) {
        if (newCantidad < 0) throw new IllegalArgumentException("cantidad no puede ser negativa");
        this.cantidad = newCantidad;
        this.updatedAt = Instant.now();
    }

    public UUID getId()           { return id; }
    public UUID getProductoId()   { return productoId; }
    public int getCantidad()      { return cantidad; }
    public Instant getUpdatedAt() { return updatedAt; }
}

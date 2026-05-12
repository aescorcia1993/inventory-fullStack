package com.inventory.purchase.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Purchase {

    private final UUID id;
    private final UUID productoId;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal total;
    private final Instant createdAt;

    public Purchase(UUID id, UUID productoId, int cantidad, BigDecimal precioUnitario) {
        if (productoId == null) throw new IllegalArgumentException("productoId es requerido");
        if (cantidad <= 0) throw new IllegalArgumentException("cantidad debe ser mayor a 0");
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("precioUnitario debe ser mayor a 0");

        this.id = id != null ? id : UUID.randomUUID();
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.total = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        this.createdAt = Instant.now();
    }

    public UUID getId()                   { return id; }
    public UUID getProductoId()           { return productoId; }
    public int getCantidad()              { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getTotal()          { return total; }
    public Instant getCreatedAt()         { return createdAt; }
}

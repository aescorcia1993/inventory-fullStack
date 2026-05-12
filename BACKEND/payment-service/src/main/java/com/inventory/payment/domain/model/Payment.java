package com.inventory.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {
    private final UUID id;
    private final UUID purchaseId;
    private final UUID productoId;
    private final int cantidad;
    private final BigDecimal total;
    private String status;
    private final Instant receivedAt;
    private Instant processedAt;

    public Payment(UUID id, UUID purchaseId, UUID productoId, int cantidad,
                   BigDecimal total, String status, Instant receivedAt, Instant processedAt) {
        this.id = id;
        this.purchaseId = purchaseId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.total = total;
        this.status = status;
        this.receivedAt = receivedAt;
        this.processedAt = processedAt;
    }

    public UUID getId()              { return id; }
    public UUID getPurchaseId()      { return purchaseId; }
    public UUID getProductoId()      { return productoId; }
    public int getCantidad()         { return cantidad; }
    public BigDecimal getTotal()     { return total; }
    public String getStatus()        { return status; }
    public Instant getReceivedAt()   { return receivedAt; }
    public Instant getProcessedAt()  { return processedAt; }
    public void setStatus(String s)       { this.status = s; }
    public void setProcessedAt(Instant i) { this.processedAt = i; }
}

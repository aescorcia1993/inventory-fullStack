package com.inventory.inventory.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class InventoryEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "producto_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID productoId;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (updatedAt == null) updatedAt = Instant.now();
    }
}

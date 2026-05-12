package com.inventory.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataInventoryRepo extends JpaRepository<InventoryEntity, UUID> {
    Optional<InventoryEntity> findByProductoId(UUID productoId);
}

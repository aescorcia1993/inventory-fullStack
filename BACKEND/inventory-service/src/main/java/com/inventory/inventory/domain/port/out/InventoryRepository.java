package com.inventory.inventory.domain.port.out;

import com.inventory.inventory.domain.model.Inventory;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {
    Inventory save(Inventory inventory);
    Optional<Inventory> findByProductoId(UUID productoId);
}

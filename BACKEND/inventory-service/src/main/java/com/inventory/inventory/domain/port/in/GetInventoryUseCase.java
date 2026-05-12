package com.inventory.inventory.domain.port.in;

import com.inventory.inventory.domain.model.Inventory;

import java.util.UUID;

public interface GetInventoryUseCase {
    Inventory execute(UUID productoId);
}

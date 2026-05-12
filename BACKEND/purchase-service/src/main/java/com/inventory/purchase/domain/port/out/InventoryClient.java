package com.inventory.purchase.domain.port.out;

import java.util.UUID;

public interface InventoryClient {
    int getStock(UUID productoId);
    void decrementStock(UUID productoId, int amount);
}

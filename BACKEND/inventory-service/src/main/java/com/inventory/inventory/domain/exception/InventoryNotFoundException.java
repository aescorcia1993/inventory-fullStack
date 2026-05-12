package com.inventory.inventory.domain.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String productoId) {
        super("Inventario no encontrado para producto: " + productoId);
    }
}

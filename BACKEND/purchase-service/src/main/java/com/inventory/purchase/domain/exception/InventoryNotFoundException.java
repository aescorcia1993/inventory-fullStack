package com.inventory.purchase.domain.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String id) {
        super("Inventario no encontrado para producto: " + id);
    }
}

package com.inventory.purchase.domain.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String id) {
        super("Producto no encontrado: " + id);
    }
}

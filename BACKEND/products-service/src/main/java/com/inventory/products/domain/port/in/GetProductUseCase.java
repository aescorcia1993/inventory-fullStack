package com.inventory.products.domain.port.in;

import com.inventory.products.domain.model.Product;

import java.util.UUID;

public interface GetProductUseCase {
    Product execute(UUID id);
}

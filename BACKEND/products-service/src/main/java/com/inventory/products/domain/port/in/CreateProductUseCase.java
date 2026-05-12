package com.inventory.products.domain.port.in;

import com.inventory.products.application.command.CreateProductCommand;
import com.inventory.products.domain.model.Product;

public interface CreateProductUseCase {
    Product execute(CreateProductCommand command);
}

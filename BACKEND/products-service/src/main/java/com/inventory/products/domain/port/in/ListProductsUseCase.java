package com.inventory.products.domain.port.in;

import com.inventory.products.domain.model.Product;

import java.util.List;

public interface ListProductsUseCase {
    List<Product> execute();
}

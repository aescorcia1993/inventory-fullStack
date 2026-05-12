package com.inventory.products.application;

import com.inventory.products.domain.model.Product;
import com.inventory.products.domain.port.in.ListProductsUseCase;
import com.inventory.products.domain.port.out.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListProductsUseCaseImpl implements ListProductsUseCase {

    private final ProductRepository productRepository;

    public ListProductsUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> execute() {
        return productRepository.findAll();
    }
}

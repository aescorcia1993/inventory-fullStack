package com.inventory.products.application;

import com.inventory.products.domain.exception.ProductNotFoundException;
import com.inventory.products.domain.model.Product;
import com.inventory.products.domain.port.in.GetProductUseCase;
import com.inventory.products.domain.port.out.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetProductUseCaseImpl implements GetProductUseCase {

    private final ProductRepository productRepository;

    public GetProductUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product execute(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id.toString()));
    }
}

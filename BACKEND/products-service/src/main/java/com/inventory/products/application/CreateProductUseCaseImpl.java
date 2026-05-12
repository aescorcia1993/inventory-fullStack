package com.inventory.products.application;

import com.inventory.products.application.command.CreateProductCommand;
import com.inventory.products.domain.model.Product;
import com.inventory.products.domain.port.in.CreateProductUseCase;
import com.inventory.products.domain.port.out.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateProductUseCaseImpl implements CreateProductUseCase {

    private final ProductRepository productRepository;

    public CreateProductUseCaseImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product execute(CreateProductCommand command) {
        Product product = new Product(null, command.nombre(), command.precio(), command.descripcion());
        return productRepository.save(product);
    }
}

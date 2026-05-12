package com.inventory.products.infrastructure.persistence;

import com.inventory.products.domain.model.Product;
import com.inventory.products.domain.port.out.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductRepo jpa;

    public JpaProductRepositoryAdapter(SpringDataProductRepo jpa) {
        this.jpa = jpa;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = toEntity(product);
        ProductEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return jpa.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private ProductEntity toEntity(Product p) {
        ProductEntity e = new ProductEntity();
        e.setId(p.getId());
        e.setNombre(p.getNombre());
        e.setPrecio(p.getPrecio());
        e.setDescripcion(p.getDescripcion());
        return e;
    }

    private Product toDomain(ProductEntity e) {
        return new Product(e.getId(), e.getNombre(), e.getPrecio(), e.getDescripcion());
    }
}

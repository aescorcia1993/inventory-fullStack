package com.inventory.products.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataProductRepo extends JpaRepository<ProductEntity, UUID> {}

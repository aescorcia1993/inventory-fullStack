package com.inventory.purchase.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataPurchaseRepo extends JpaRepository<PurchaseEntity, UUID> {}

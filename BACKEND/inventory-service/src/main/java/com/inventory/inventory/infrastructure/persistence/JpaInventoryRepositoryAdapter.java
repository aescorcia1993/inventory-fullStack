package com.inventory.inventory.infrastructure.persistence;

import com.inventory.inventory.domain.model.Inventory;
import com.inventory.inventory.domain.port.out.InventoryRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaInventoryRepositoryAdapter implements InventoryRepository {

    private final SpringDataInventoryRepo springDataRepo;

    public JpaInventoryRepositoryAdapter(SpringDataInventoryRepo springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity entity = toEntity(inventory);
        InventoryEntity saved = springDataRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Inventory> findByProductoId(UUID productoId) {
        return springDataRepo.findByProductoId(productoId).map(this::toDomain);
    }

    private InventoryEntity toEntity(Inventory inv) {
        InventoryEntity e = new InventoryEntity();
        e.setId(inv.getId());
        e.setProductoId(inv.getProductoId());
        e.setCantidad(inv.getCantidad());
        e.setUpdatedAt(inv.getUpdatedAt());
        return e;
    }

    private Inventory toDomain(InventoryEntity e) {
        return new Inventory(e.getId(), e.getProductoId(), e.getCantidad());
    }
}

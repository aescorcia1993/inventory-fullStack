package com.inventory.purchase.infrastructure.persistence;

import com.inventory.purchase.domain.model.Purchase;
import com.inventory.purchase.domain.port.out.PurchaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaPurchaseRepositoryAdapter implements PurchaseRepository {

    private final SpringDataPurchaseRepo springDataRepo;

    public JpaPurchaseRepositoryAdapter(SpringDataPurchaseRepo springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Purchase save(Purchase purchase) {
        PurchaseEntity entity = toEntity(purchase);
        PurchaseEntity saved = springDataRepo.save(entity);
        return toDomain(saved);
    }

    private PurchaseEntity toEntity(Purchase p) {
        PurchaseEntity e = new PurchaseEntity();
        e.setId(p.getId());
        e.setProductoId(p.getProductoId());
        e.setCantidad(p.getCantidad());
        e.setPrecioUnitario(p.getPrecioUnitario());
        e.setTotal(p.getTotal());
        e.setCreatedAt(p.getCreatedAt());
        return e;
    }

    private Purchase toDomain(PurchaseEntity e) {
        return new Purchase(e.getId(), e.getProductoId(), e.getCantidad(), e.getPrecioUnitario());
    }
}

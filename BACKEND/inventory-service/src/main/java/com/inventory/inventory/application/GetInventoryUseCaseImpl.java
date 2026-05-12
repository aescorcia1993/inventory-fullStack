package com.inventory.inventory.application;

import com.inventory.inventory.domain.exception.InventoryNotFoundException;
import com.inventory.inventory.domain.model.Inventory;
import com.inventory.inventory.domain.port.in.GetInventoryUseCase;
import com.inventory.inventory.domain.port.out.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetInventoryUseCaseImpl implements GetInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public GetInventoryUseCaseImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Inventory execute(UUID productoId) {
        return inventoryRepository.findByProductoId(productoId)
                .orElseThrow(() -> new InventoryNotFoundException(productoId.toString()));
    }
}

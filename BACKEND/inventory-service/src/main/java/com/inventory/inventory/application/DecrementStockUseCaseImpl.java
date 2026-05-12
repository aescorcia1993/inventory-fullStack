package com.inventory.inventory.application;

import com.inventory.inventory.application.command.DecrementStockCommand;
import com.inventory.inventory.domain.exception.InventoryNotFoundException;
import com.inventory.inventory.domain.model.Inventory;
import com.inventory.inventory.domain.port.in.DecrementStockUseCase;
import com.inventory.inventory.domain.port.out.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DecrementStockUseCaseImpl implements DecrementStockUseCase {

    private final InventoryRepository inventoryRepository;

    public DecrementStockUseCaseImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Inventory execute(DecrementStockCommand command) {
        Inventory inventory = inventoryRepository.findByProductoId(command.productoId())
                .orElseThrow(() -> new InventoryNotFoundException(command.productoId().toString()));
        inventory.decrement(command.amount());
        return inventoryRepository.save(inventory);
    }
}

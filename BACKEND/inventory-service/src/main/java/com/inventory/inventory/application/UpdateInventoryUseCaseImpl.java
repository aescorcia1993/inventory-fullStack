package com.inventory.inventory.application;

import com.inventory.inventory.application.command.UpdateInventoryCommand;
import com.inventory.inventory.domain.exception.InventoryNotFoundException;
import com.inventory.inventory.domain.model.Inventory;
import com.inventory.inventory.domain.port.in.UpdateInventoryUseCase;
import com.inventory.inventory.domain.port.out.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateInventoryUseCaseImpl implements UpdateInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public UpdateInventoryUseCaseImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Inventory execute(UpdateInventoryCommand command) {
        Inventory inventory = inventoryRepository.findByProductoId(command.productoId())
                .orElseThrow(() -> new InventoryNotFoundException(command.productoId().toString()));
        inventory.updateCantidad(command.cantidad());
        return inventoryRepository.save(inventory);
    }
}

package com.inventory.inventory.application;

import com.inventory.inventory.application.command.CreateInventoryCommand;
import com.inventory.inventory.domain.model.Inventory;
import com.inventory.inventory.domain.port.in.CreateInventoryUseCase;
import com.inventory.inventory.domain.port.out.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateInventoryUseCaseImpl implements CreateInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public CreateInventoryUseCaseImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Inventory execute(CreateInventoryCommand command) {
        Inventory inventory = new Inventory(null, command.productoId(), command.cantidad());
        return inventoryRepository.save(inventory);
    }
}

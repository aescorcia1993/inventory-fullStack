package com.inventory.inventory.domain.port.in;

import com.inventory.inventory.application.command.CreateInventoryCommand;
import com.inventory.inventory.domain.model.Inventory;

public interface CreateInventoryUseCase {
    Inventory execute(CreateInventoryCommand command);
}

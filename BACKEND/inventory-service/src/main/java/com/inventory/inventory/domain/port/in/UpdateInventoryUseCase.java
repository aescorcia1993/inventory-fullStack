package com.inventory.inventory.domain.port.in;

import com.inventory.inventory.application.command.UpdateInventoryCommand;
import com.inventory.inventory.domain.model.Inventory;

public interface UpdateInventoryUseCase {
    Inventory execute(UpdateInventoryCommand command);
}

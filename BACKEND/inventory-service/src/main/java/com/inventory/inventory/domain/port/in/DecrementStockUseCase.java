package com.inventory.inventory.domain.port.in;

import com.inventory.inventory.application.command.DecrementStockCommand;
import com.inventory.inventory.domain.model.Inventory;

public interface DecrementStockUseCase {
    Inventory execute(DecrementStockCommand command);
}

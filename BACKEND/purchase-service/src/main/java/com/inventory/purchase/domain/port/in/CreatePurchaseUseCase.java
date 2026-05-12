package com.inventory.purchase.domain.port.in;

import com.inventory.purchase.application.command.CreatePurchaseCommand;
import com.inventory.purchase.domain.model.Purchase;

public interface CreatePurchaseUseCase {
    Purchase execute(CreatePurchaseCommand command);
}

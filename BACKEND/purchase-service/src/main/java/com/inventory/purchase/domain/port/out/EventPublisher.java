package com.inventory.purchase.domain.port.out;

import com.inventory.purchase.domain.model.Purchase;

public interface EventPublisher {
    void publishPurchaseCompleted(Purchase purchase);
}

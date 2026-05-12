package com.inventory.purchase.application;

import com.inventory.purchase.application.command.CreatePurchaseCommand;
import com.inventory.purchase.domain.exception.InsufficientStockException;
import com.inventory.purchase.domain.model.Purchase;
import com.inventory.purchase.domain.port.in.CreatePurchaseUseCase;
import com.inventory.purchase.domain.port.out.EventPublisher;
import com.inventory.purchase.domain.port.out.InventoryClient;
import com.inventory.purchase.domain.port.out.ProductClient;
import com.inventory.purchase.domain.port.out.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class CreatePurchaseUseCaseImpl implements CreatePurchaseUseCase {

    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final PurchaseRepository purchaseRepository;
    private final EventPublisher eventPublisher;

    public CreatePurchaseUseCaseImpl(ProductClient productClient,
                                     InventoryClient inventoryClient,
                                     PurchaseRepository purchaseRepository,
                                     EventPublisher eventPublisher) {
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
        this.purchaseRepository = purchaseRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Purchase execute(CreatePurchaseCommand command) {
        // 1. Get product price (also validates product exists)
        BigDecimal price = productClient.getProductPrice(command.productoId());

        // 2. Check stock
        int stock = inventoryClient.getStock(command.productoId());
        if (stock < command.cantidad()) {
            throw new InsufficientStockException(
                    "Stock insuficiente. Disponible: " + stock + ", solicitado: " + command.cantidad());
        }

        // 3. Decrement stock
        inventoryClient.decrementStock(command.productoId(), command.cantidad());

        // 4. Save purchase
        Purchase purchase = new Purchase(null, command.productoId(), command.cantidad(), price);
        Purchase saved = purchaseRepository.save(purchase);

        // 5. Publish event
        eventPublisher.publishPurchaseCompleted(saved);

        return saved;
    }
}

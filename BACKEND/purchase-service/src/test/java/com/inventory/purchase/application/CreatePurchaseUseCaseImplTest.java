package com.inventory.purchase.application;

import com.inventory.purchase.application.command.CreatePurchaseCommand;
import com.inventory.purchase.domain.exception.InsufficientStockException;
import com.inventory.purchase.domain.exception.ProductNotFoundException;
import com.inventory.purchase.domain.model.Purchase;
import com.inventory.purchase.domain.port.out.EventPublisher;
import com.inventory.purchase.domain.port.out.InventoryClient;
import com.inventory.purchase.domain.port.out.ProductClient;
import com.inventory.purchase.domain.port.out.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePurchaseUseCaseImplTest {

    @Mock ProductClient productClient;
    @Mock InventoryClient inventoryClient;
    @Mock PurchaseRepository purchaseRepository;
    @Mock EventPublisher eventPublisher;

    @InjectMocks CreatePurchaseUseCaseImpl useCase;

    private final UUID productId = UUID.randomUUID();

    @Test
    void createPurchase_success_savesAndPublishesEvent() {
        when(productClient.getProductPrice(productId)).thenReturn(new BigDecimal("29.99"));
        when(inventoryClient.getStock(productId)).thenReturn(10);
        Purchase saved = new Purchase(UUID.randomUUID(), productId, 2, new BigDecimal("29.99"));
        when(purchaseRepository.save(any())).thenReturn(saved);

        Purchase result = useCase.execute(new CreatePurchaseCommand(productId, 2));

        assertThat(result.getProductoId()).isEqualTo(productId);
        assertThat(result.getCantidad()).isEqualTo(2);
        verify(inventoryClient).decrementStock(productId, 2);
        verify(eventPublisher).publishPurchaseCompleted(saved);
    }

    @Test
    void createPurchase_productNotFound_throws() {
        when(productClient.getProductPrice(productId))
                .thenThrow(new ProductNotFoundException(productId.toString()));

        assertThatThrownBy(() -> useCase.execute(new CreatePurchaseCommand(productId, 1)))
                .isInstanceOf(ProductNotFoundException.class);
        verifyNoInteractions(purchaseRepository, eventPublisher);
    }

    @Test
    void createPurchase_insufficientStock_throws() {
        when(productClient.getProductPrice(productId)).thenReturn(new BigDecimal("10.00"));
        when(inventoryClient.getStock(productId)).thenReturn(1);

        assertThatThrownBy(() -> useCase.execute(new CreatePurchaseCommand(productId, 5)))
                .isInstanceOf(InsufficientStockException.class);
        verifyNoInteractions(purchaseRepository, eventPublisher);
        verify(inventoryClient, never()).decrementStock(any(), anyInt());
    }

    @Test
    void createPurchase_zeroQuantity_throwsFromDomain() {
        assertThatThrownBy(() -> new Purchase(null, productId, 0, new BigDecimal("10.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package com.inventory.inventory.application;

import com.inventory.inventory.application.command.CreateInventoryCommand;
import com.inventory.inventory.application.command.DecrementStockCommand;
import com.inventory.inventory.application.command.UpdateInventoryCommand;
import com.inventory.inventory.domain.exception.InventoryNotFoundException;
import com.inventory.inventory.domain.model.Inventory;
import com.inventory.inventory.domain.model.InsufficientStockException;
import com.inventory.inventory.domain.port.out.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryUseCaseImplTest {

    @Mock InventoryRepository inventoryRepository;

    @InjectMocks CreateInventoryUseCaseImpl createUseCase;

    @Test
    void createInventory_savesAndReturns() {
        UUID pid = UUID.randomUUID();
        Inventory inv = new Inventory(null, pid, 10);
        when(inventoryRepository.save(any())).thenReturn(inv);

        Inventory result = createUseCase.execute(new CreateInventoryCommand(pid, 10));

        assertThat(result.getProductoId()).isEqualTo(pid);
        assertThat(result.getCantidad()).isEqualTo(10);
        verify(inventoryRepository).save(any());
    }

    @Test
    void createInventory_negativeCantidad_throws() {
        assertThatThrownBy(() -> new Inventory(null, UUID.randomUUID(), -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativa");
    }

    @Test
    void getInventory_notFound_throws() {
        UUID pid = UUID.randomUUID();
        GetInventoryUseCaseImpl getUseCase = new GetInventoryUseCaseImpl(inventoryRepository);
        when(inventoryRepository.findByProductoId(pid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getUseCase.execute(pid))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void updateInventory_notFound_throws() {
        UUID pid = UUID.randomUUID();
        UpdateInventoryUseCaseImpl updateUseCase = new UpdateInventoryUseCaseImpl(inventoryRepository);
        when(inventoryRepository.findByProductoId(pid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateUseCase.execute(new UpdateInventoryCommand(pid, 5)))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void decrementStock_insufficientStock_throws() {
        UUID pid = UUID.randomUUID();
        Inventory inv = new Inventory(null, pid, 2);
        DecrementStockUseCaseImpl decrementUseCase = new DecrementStockUseCaseImpl(inventoryRepository);
        when(inventoryRepository.findByProductoId(pid)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> decrementUseCase.execute(new DecrementStockCommand(pid, 5)))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void decrementStock_sufficient_savesDecremented() {
        UUID pid = UUID.randomUUID();
        Inventory inv = new Inventory(null, pid, 10);
        DecrementStockUseCaseImpl decrementUseCase = new DecrementStockUseCaseImpl(inventoryRepository);
        when(inventoryRepository.findByProductoId(pid)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Inventory result = decrementUseCase.execute(new DecrementStockCommand(pid, 3));

        assertThat(result.getCantidad()).isEqualTo(7);
    }
}

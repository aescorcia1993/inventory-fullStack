package com.inventory.inventory.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.inventory.application.command.CreateInventoryCommand;
import com.inventory.inventory.application.command.DecrementStockCommand;
import com.inventory.inventory.application.command.UpdateInventoryCommand;
import com.inventory.inventory.domain.exception.InventoryNotFoundException;
import com.inventory.inventory.domain.model.Inventory;
import com.inventory.inventory.domain.model.InsufficientStockException;
import com.inventory.inventory.domain.port.in.CreateInventoryUseCase;
import com.inventory.inventory.domain.port.in.DecrementStockUseCase;
import com.inventory.inventory.domain.port.in.GetInventoryUseCase;
import com.inventory.inventory.domain.port.in.UpdateInventoryUseCase;
import com.inventory.inventory.infrastructure.web.InventoryController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean GetInventoryUseCase getInventoryUseCase;
    @MockBean UpdateInventoryUseCase updateInventoryUseCase;
    @MockBean DecrementStockUseCase decrementStockUseCase;
    @MockBean CreateInventoryUseCase createInventoryUseCase;

    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Test
    void createInventory_returns201() throws Exception {
        Inventory inv = new Inventory(UUID.randomUUID(), PRODUCT_ID, 50);
        when(createInventoryUseCase.execute(any())).thenReturn(inv);

        var body = Map.of("data", Map.of("type", "inventory",
                "attributes", Map.of("productoId", PRODUCT_ID.toString(), "cantidad", 50)));

        mockMvc.perform(post("/api/v1/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("inventory"))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(50));
    }

    @Test
    void getInventory_returns200() throws Exception {
        Inventory inv = new Inventory(UUID.randomUUID(), PRODUCT_ID, 25);
        when(getInventoryUseCase.execute(PRODUCT_ID)).thenReturn(inv);

        mockMvc.perform(get("/api/v1/inventory/" + PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes.cantidad").value(25));
    }

    @Test
    void getInventory_notFound_returns404() throws Exception {
        when(getInventoryUseCase.execute(any())).thenThrow(new InventoryNotFoundException(PRODUCT_ID.toString()));

        mockMvc.perform(get("/api/v1/inventory/" + PRODUCT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void decrement_insufficientStock_returns422() throws Exception {
        when(decrementStockUseCase.execute(any())).thenThrow(new InsufficientStockException("Stock insuficiente"));

        var body = Map.of("data", Map.of("type", "decrement",
                "attributes", Map.of("amount", 100)));

        mockMvc.perform(post("/api/v1/inventory/" + PRODUCT_ID + "/decrement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void updateInventory_returns200() throws Exception {
        Inventory updated = new Inventory(UUID.randomUUID(), PRODUCT_ID, 100);
        when(updateInventoryUseCase.execute(any())).thenReturn(updated);

        var body = Map.of("data", Map.of("type", "inventory",
                "attributes", Map.of("cantidad", 100)));

        mockMvc.perform(put("/api/v1/inventory/" + PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attributes.cantidad").value(100));
    }
}

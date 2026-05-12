package com.inventory.purchase.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.purchase.domain.exception.InsufficientStockException;
import com.inventory.purchase.domain.exception.ProductNotFoundException;
import com.inventory.purchase.domain.model.Purchase;
import com.inventory.purchase.domain.port.in.CreatePurchaseUseCase;
import com.inventory.purchase.infrastructure.web.PurchaseController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseController.class)
@AutoConfigureMockMvc(addFilters = false)
class PurchaseControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @MockBean CreatePurchaseUseCase createPurchaseUseCase;

    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Test
    void createPurchase_returns201() throws Exception {
        Purchase purchase = new Purchase(UUID.randomUUID(), PRODUCT_ID, 2, new BigDecimal("19.99"));
        when(createPurchaseUseCase.execute(any())).thenReturn(purchase);

        var body = Map.of("data", Map.of("type", "purchases",
                "attributes", Map.of("productoId", PRODUCT_ID.toString(), "cantidad", 2)));

        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("purchases"))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(2));
    }

    @Test
    void createPurchase_missingProductoId_returns422() throws Exception {
        var body = Map.of("data", Map.of("type", "purchases",
                "attributes", Map.of("cantidad", 2)));

        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createPurchase_productNotFound_returns404() throws Exception {
        when(createPurchaseUseCase.execute(any()))
                .thenThrow(new ProductNotFoundException(PRODUCT_ID.toString()));

        var body = Map.of("data", Map.of("type", "purchases",
                "attributes", Map.of("productoId", PRODUCT_ID.toString(), "cantidad", 1)));

        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPurchase_insufficientStock_returns422() throws Exception {
        when(createPurchaseUseCase.execute(any()))
                .thenThrow(new InsufficientStockException("Stock insuficiente"));

        var body = Map.of("data", Map.of("type", "purchases",
                "attributes", Map.of("productoId", PRODUCT_ID.toString(), "cantidad", 100)));

        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }
}

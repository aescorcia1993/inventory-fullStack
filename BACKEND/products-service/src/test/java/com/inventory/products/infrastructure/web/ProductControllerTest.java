package com.inventory.products.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.products.domain.exception.ProductNotFoundException;
import com.inventory.products.domain.model.Product;
import com.inventory.products.domain.port.in.CreateProductUseCase;
import com.inventory.products.domain.port.in.GetProductUseCase;
import com.inventory.products.domain.port.in.ListProductsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateProductUseCase createProductUseCase;

    @MockBean
    private GetProductUseCase getProductUseCase;

    @MockBean
    private ListProductsUseCase listProductsUseCase;

    @Test
    void createProduct_withValidBody_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        Product product = new Product(id, "Widget", new BigDecimal("9.99"), "desc");
        when(createProductUseCase.execute(any())).thenReturn(product);

        var body = Map.of("data", Map.of(
                "type", "products",
                "attributes", Map.of("nombre", "Widget", "precio", 9.99)));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.api+json"))
                .andExpect(jsonPath("$.data.type").value("products"))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.attributes.nombre").value("Widget"));
    }

    @Test
    void createProduct_withMissingNombre_returns422() throws Exception {
        var body = Map.of("data", Map.of(
                "type", "products",
                "attributes", Map.of("precio", 9.99)));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].status").value("422"));
    }

    @Test
    void getProduct_whenExists_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(getProductUseCase.execute(id)).thenReturn(new Product(id, "Widget", new BigDecimal("9.99"), null));

        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    void getProduct_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(getProductUseCase.execute(id)).thenThrow(new ProductNotFoundException(id.toString()));

        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].status").value("404"));
    }

    @Test
    void listProducts_returnsJsonApiList() throws Exception {
        UUID id = UUID.randomUUID();
        when(listProductsUseCase.execute()).thenReturn(
                List.of(new Product(id, "Widget", new BigDecimal("9.99"), null)));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("products"))
                .andExpect(jsonPath("$.data[0].attributes.nombre").value("Widget"));
    }
}

package com.inventory.purchase.integration;

import com.inventory.purchase.domain.port.out.EventPublisher;
import com.inventory.purchase.domain.port.out.InventoryClient;
import com.inventory.purchase.domain.port.out.ProductClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PurchaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.api-key", () -> "test-key");
        // Prevent actual RabbitMQ/service connections
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "5672");
        registry.add("services.products.url", () -> "http://localhost:9999");
        registry.add("services.inventory.url", () -> "http://localhost:9998");
    }

    @MockBean ProductClient productClient;
    @MockBean InventoryClient inventoryClient;
    @MockBean EventPublisher eventPublisher;

    @Autowired TestRestTemplate restTemplate;

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-API-Key", "test-key");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void createPurchase_success_returns201() {
        UUID productId = UUID.randomUUID();
        when(productClient.getProductPrice(productId)).thenReturn(new BigDecimal("25.00"));
        when(inventoryClient.getStock(productId)).thenReturn(20);
        doNothing().when(inventoryClient).decrementStock(any(), anyInt());
        doNothing().when(eventPublisher).publishPurchaseCompleted(any());

        var body = Map.of("data", Map.of("type", "purchases",
                "attributes", Map.of("productoId", productId.toString(), "cantidad", 3)));

        ResponseEntity<Map> response = restTemplate.exchange("/api/v1/purchases",
                HttpMethod.POST, new HttpEntity<>(body, headers()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<?, ?> attrs = (Map<?, ?>) ((Map<?, ?>) response.getBody().get("data")).get("attributes");
        assertThat(((Number) attrs.get("cantidad")).intValue()).isEqualTo(3);
        verify(eventPublisher).publishPurchaseCompleted(any());
    }

    @Test
    void createPurchase_invalidApiKey_returns401() {
        HttpHeaders bad = new HttpHeaders();
        bad.set("X-API-Key", "wrong");
        bad.setContentType(MediaType.APPLICATION_JSON);

        var body = Map.of("data", Map.of("type", "purchases",
                "attributes", Map.of("productoId", UUID.randomUUID().toString(), "cantidad", 1)));

        ResponseEntity<String> response = restTemplate.exchange("/api/v1/purchases",
                HttpMethod.POST, new HttpEntity<>(body, bad), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}

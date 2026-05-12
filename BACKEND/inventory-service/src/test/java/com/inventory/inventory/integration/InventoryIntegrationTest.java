package com.inventory.inventory.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class InventoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.api-key", () -> "test-key");
    }

    @Autowired TestRestTemplate restTemplate;

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-API-Key", "test-key");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void createInventory_thenGet_returnsConsistentData() {
        UUID productId = UUID.randomUUID();
        var body = Map.of("data", Map.of("type", "inventory",
                "attributes", Map.of("productoId", productId.toString(), "cantidad", 100)));

        ResponseEntity<Map> created = restTemplate.exchange("/api/v1/inventory",
                HttpMethod.POST, new HttpEntity<>(body, headers()), Map.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Map> fetched = restTemplate.exchange("/api/v1/inventory/" + productId,
                HttpMethod.GET, new HttpEntity<>(headers()), Map.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?, ?> attrs = (Map<?, ?>) ((Map<?, ?>) fetched.getBody().get("data")).get("attributes");
        assertThat(attrs.get("cantidad")).isEqualTo(100);
    }

    @Test
    void decrement_belowZero_returns422() {
        UUID productId = UUID.randomUUID();
        var createBody = Map.of("data", Map.of("type", "inventory",
                "attributes", Map.of("productoId", productId.toString(), "cantidad", 5)));
        restTemplate.exchange("/api/v1/inventory", HttpMethod.POST,
                new HttpEntity<>(createBody, headers()), Map.class);

        var decrementBody = Map.of("data", Map.of("type", "decrement",
                "attributes", Map.of("amount", 10)));
        ResponseEntity<Map> result = restTemplate.exchange(
                "/api/v1/inventory/" + productId + "/decrement",
                HttpMethod.POST, new HttpEntity<>(decrementBody, headers()), Map.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void getInventory_invalidApiKey_returns401() {
        HttpHeaders bad = new HttpHeaders();
        bad.set("X-API-Key", "bad-key");
        ResponseEntity<String> result = restTemplate.exchange(
                "/api/v1/inventory/" + UUID.randomUUID(),
                HttpMethod.GET, new HttpEntity<>(bad), String.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}

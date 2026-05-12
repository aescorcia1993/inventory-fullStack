package com.inventory.products.integration;

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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.api-key", () -> "test-api-key");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-API-Key", "test-api-key");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void createProduct_thenGetById_returnsConsistentData() {
        var body = Map.of("data", Map.of(
                "type", "products",
                "attributes", Map.of("nombre", "Integration Widget", "precio", 19.99)));

        ResponseEntity<Map> created = restTemplate.exchange(
                "/api/v1/products", HttpMethod.POST,
                new HttpEntity<>(body, headers()), Map.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String id = (String) ((Map<?, ?>) created.getBody().get("data")).get("id");
        assertThat(id).isNotNull();

        ResponseEntity<Map> fetched = restTemplate.exchange(
                "/api/v1/products/" + id, HttpMethod.GET,
                new HttpEntity<>(headers()), Map.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<?, ?> attributes = (Map<?, ?>) ((Map<?, ?>) fetched.getBody().get("data")).get("attributes");
        assertThat(attributes.get("nombre")).isEqualTo("Integration Widget");
    }

    @Test
    void getProduct_withInvalidApiKey_returns401() {
        HttpHeaders badHeaders = new HttpHeaders();
        badHeaders.set("X-API-Key", "wrong-key");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/products/" + java.util.UUID.randomUUID(),
                HttpMethod.GET, new HttpEntity<>(badHeaders), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getProduct_whenNotFound_returns404() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/products/" + java.util.UUID.randomUUID(),
                HttpMethod.GET, new HttpEntity<>(headers()), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

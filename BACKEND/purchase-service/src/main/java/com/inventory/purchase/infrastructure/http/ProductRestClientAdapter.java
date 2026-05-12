package com.inventory.purchase.infrastructure.http;

import com.inventory.purchase.domain.exception.ProductNotFoundException;
import com.inventory.purchase.domain.exception.ServiceCommunicationException;
import com.inventory.purchase.domain.port.out.ProductClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class ProductRestClientAdapter implements ProductClient {

    private final RestClient restClient;

    public ProductRestClientAdapter(
            @Value("${services.products.url}") String baseUrl,
            @Value("${services.products.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public BigDecimal getProductPrice(UUID productoId) {
        try {
            Map<?, ?> response = restClient.get()
                    .uri("/api/v1/products/{id}", productoId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        if (res.getStatusCode().value() == 404) {
                            throw new ProductNotFoundException(productoId.toString());
                        }
                        throw new ServiceCommunicationException("Products service error: " + res.getStatusCode(), null);
                    })
                    .body(Map.class);

            Map<?, ?> data = (Map<?, ?>) response.get("data");
            Map<?, ?> attributes = (Map<?, ?>) data.get("attributes");
            return new BigDecimal(attributes.get("precio").toString());
        } catch (ProductNotFoundException | ServiceCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceCommunicationException("Error communicating with products service", e);
        }
    }
}

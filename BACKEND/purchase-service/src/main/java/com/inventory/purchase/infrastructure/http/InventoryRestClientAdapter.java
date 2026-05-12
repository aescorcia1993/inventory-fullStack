package com.inventory.purchase.infrastructure.http;

import com.inventory.purchase.domain.exception.InventoryNotFoundException;
import com.inventory.purchase.domain.exception.ServiceCommunicationException;
import com.inventory.purchase.domain.port.out.InventoryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class InventoryRestClientAdapter implements InventoryClient {

    private final RestClient restClient;

    public InventoryRestClientAdapter(
            @Value("${services.inventory.url}") String baseUrl,
            @Value("${services.inventory.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public int getStock(UUID productoId) {
        try {
            Map<?, ?> response = restClient.get()
                    .uri("/api/v1/inventory/{productoId}", productoId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        if (res.getStatusCode().value() == 404) {
                            throw new InventoryNotFoundException(productoId.toString());
                        }
                        throw new ServiceCommunicationException("Inventory service error: " + res.getStatusCode(), null);
                    })
                    .body(Map.class);

            Map<?, ?> data = (Map<?, ?>) response.get("data");
            Map<?, ?> attributes = (Map<?, ?>) data.get("attributes");
            return ((Number) attributes.get("cantidad")).intValue();
        } catch (InventoryNotFoundException | ServiceCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceCommunicationException("Error communicating with inventory service", e);
        }
    }

    @Override
    public void decrementStock(UUID productoId, int amount) {
        try {
            var body = Map.of("data", Map.of("type", "decrement",
                    "attributes", Map.of("amount", amount)));
            restClient.post()
                    .uri("/api/v1/inventory/{productoId}/decrement", productoId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new ServiceCommunicationException(
                                "Failed to decrement stock: " + res.getStatusCode(), null);
                    })
                    .toBodilessEntity();
        } catch (ServiceCommunicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceCommunicationException("Error communicating with inventory service", e);
        }
    }
}

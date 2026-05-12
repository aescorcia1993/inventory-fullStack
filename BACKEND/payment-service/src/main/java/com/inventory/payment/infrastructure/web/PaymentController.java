package com.inventory.payment.infrastructure.web;

import com.inventory.payment.infrastructure.persistence.PaymentRepository;
import com.inventory.payment.infrastructure.web.dto.PaymentAttributes;
import com.inventory.payment.infrastructure.web.jsonapi.JsonApiData;
import com.inventory.payment.infrastructure.web.jsonapi.JsonApiListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/payments", produces = "application/vnd.api+json")
@SecurityRequirement(name = "ApiKeyAuth")
public class PaymentController {

    private static final String TYPE = "payments";

    private final PaymentRepository repository;

    public PaymentController(PaymentRepository repository) {
        this.repository = repository;
    }

    @Operation(summary = "List last 100 payments ordered by received date desc")
    @GetMapping
    public JsonApiListResponse<PaymentAttributes> list() {
        List<JsonApiData<PaymentAttributes>> data = repository
                .findTop100ByOrderByReceivedAtDesc()
                .stream()
                .map(e -> new JsonApiData<>(TYPE, e.getId().toString(),
                        new PaymentAttributes(
                                e.getPurchaseId(),
                                e.getProductoId(),
                                e.getCantidad(),
                                e.getTotal(),
                                e.getStatus(),
                                e.getReceivedAt(),
                                e.getProcessedAt())))
                .toList();
        return new JsonApiListResponse<>(data);
    }
}

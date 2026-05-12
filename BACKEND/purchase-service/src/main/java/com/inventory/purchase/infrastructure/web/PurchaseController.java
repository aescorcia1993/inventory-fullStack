package com.inventory.purchase.infrastructure.web;

import com.inventory.purchase.application.command.CreatePurchaseCommand;
import com.inventory.purchase.domain.model.Purchase;
import com.inventory.purchase.domain.port.in.CreatePurchaseUseCase;
import com.inventory.purchase.infrastructure.web.dto.CreatePurchaseRequest;
import com.inventory.purchase.infrastructure.web.dto.PurchaseAttributes;
import com.inventory.purchase.infrastructure.web.jsonapi.JsonApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/purchases", produces = "application/vnd.api+json")
@SecurityRequirement(name = "ApiKeyAuth")
public class PurchaseController {

    private static final String TYPE = "purchases";
    private final CreatePurchaseUseCase createPurchaseUseCase;

    public PurchaseController(CreatePurchaseUseCase createPurchaseUseCase) {
        this.createPurchaseUseCase = createPurchaseUseCase;
    }

    @Operation(summary = "Create a new purchase")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public JsonApiResponse<PurchaseAttributes> create(@Valid @RequestBody CreatePurchaseRequest request) {
        var attrs = request.data().attributes();
        Purchase purchase = createPurchaseUseCase.execute(
                new CreatePurchaseCommand(attrs.productoId(), attrs.cantidad()));
        return JsonApiResponse.of(TYPE, purchase.getId().toString(),
                new PurchaseAttributes(purchase.getProductoId(), purchase.getCantidad(),
                        purchase.getPrecioUnitario(), purchase.getTotal(), purchase.getCreatedAt()));
    }
}

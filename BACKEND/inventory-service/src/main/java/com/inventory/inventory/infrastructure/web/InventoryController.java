package com.inventory.inventory.infrastructure.web;

import com.inventory.inventory.application.command.CreateInventoryCommand;
import com.inventory.inventory.application.command.DecrementStockCommand;
import com.inventory.inventory.application.command.UpdateInventoryCommand;
import com.inventory.inventory.domain.model.Inventory;
import com.inventory.inventory.domain.port.in.CreateInventoryUseCase;
import com.inventory.inventory.domain.port.in.DecrementStockUseCase;
import com.inventory.inventory.domain.port.in.GetInventoryUseCase;
import com.inventory.inventory.domain.port.in.UpdateInventoryUseCase;
import com.inventory.inventory.infrastructure.web.dto.*;
import com.inventory.inventory.infrastructure.web.jsonapi.JsonApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/inventory", produces = "application/vnd.api+json")
@SecurityRequirement(name = "ApiKeyAuth")
public class InventoryController {

    private static final String TYPE = "inventory";

    private final GetInventoryUseCase getInventoryUseCase;
    private final UpdateInventoryUseCase updateInventoryUseCase;
    private final DecrementStockUseCase decrementStockUseCase;
    private final CreateInventoryUseCase createInventoryUseCase;

    public InventoryController(GetInventoryUseCase getInventoryUseCase,
                               UpdateInventoryUseCase updateInventoryUseCase,
                               DecrementStockUseCase decrementStockUseCase,
                               CreateInventoryUseCase createInventoryUseCase) {
        this.getInventoryUseCase = getInventoryUseCase;
        this.updateInventoryUseCase = updateInventoryUseCase;
        this.decrementStockUseCase = decrementStockUseCase;
        this.createInventoryUseCase = createInventoryUseCase;
    }

    @Operation(summary = "Initialize inventory for a product")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public JsonApiResponse<InventoryAttributes> create(@Valid @RequestBody CreateInventoryRequest request) {
        var attrs = request.data().attributes();
        Inventory inv = createInventoryUseCase.execute(
                new CreateInventoryCommand(attrs.productoId(), attrs.cantidad()));
        return toResponse(inv);
    }

    @Operation(summary = "Get inventory by product id")
    @GetMapping("/{productoId}")
    public JsonApiResponse<InventoryAttributes> getByProductoId(@PathVariable UUID productoId) {
        Inventory inv = getInventoryUseCase.execute(productoId);
        return toResponse(inv);
    }

    @Operation(summary = "Set stock level for a product")
    @PutMapping(value = "/{productoId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public JsonApiResponse<InventoryAttributes> update(@PathVariable UUID productoId,
                                                       @Valid @RequestBody UpdateInventoryRequest request) {
        Inventory inv = updateInventoryUseCase.execute(
                new UpdateInventoryCommand(productoId, request.data().attributes().cantidad()));
        return toResponse(inv);
    }

    @Operation(summary = "Decrement stock (called by purchase-service)")
    @PostMapping(value = "/{productoId}/decrement", consumes = MediaType.APPLICATION_JSON_VALUE)
    public JsonApiResponse<InventoryAttributes> decrement(@PathVariable UUID productoId,
                                                          @Valid @RequestBody DecrementStockRequest request) {
        Inventory inv = decrementStockUseCase.execute(
                new DecrementStockCommand(productoId, request.data().attributes().amount()));
        return toResponse(inv);
    }

    private JsonApiResponse<InventoryAttributes> toResponse(Inventory inv) {
        return JsonApiResponse.of(TYPE, inv.getId().toString(),
                new InventoryAttributes(inv.getProductoId(), inv.getCantidad(), inv.getUpdatedAt()));
    }
}

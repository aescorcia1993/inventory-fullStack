package com.inventory.products.infrastructure.web;

import com.inventory.products.application.command.CreateProductCommand;
import com.inventory.products.domain.model.Product;
import com.inventory.products.domain.port.in.CreateProductUseCase;
import com.inventory.products.domain.port.in.GetProductUseCase;
import com.inventory.products.domain.port.in.ListProductsUseCase;
import com.inventory.products.infrastructure.web.dto.CreateProductRequest;
import com.inventory.products.infrastructure.web.dto.ProductAttributes;
import com.inventory.products.infrastructure.web.jsonapi.JsonApiData;
import com.inventory.products.infrastructure.web.jsonapi.JsonApiListResponse;
import com.inventory.products.infrastructure.web.jsonapi.JsonApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Gestión de productos")
@SecurityRequirement(name = "ApiKeyAuth")
public class ProductController {

    private static final String MEDIA_TYPE = "application/vnd.api+json";
    private static final String TYPE = "products";

    private final CreateProductUseCase createProduct;
    private final GetProductUseCase getProduct;
    private final ListProductsUseCase listProducts;

    public ProductController(CreateProductUseCase createProduct,
                             GetProductUseCase getProduct,
                             ListProductsUseCase listProducts) {
        this.createProduct = createProduct;
        this.getProduct = getProduct;
        this.listProducts = listProducts;
    }

    @PostMapping(produces = MEDIA_TYPE)
    @Operation(summary = "Crear un nuevo producto")
    public ResponseEntity<JsonApiResponse<ProductAttributes>> create(
            @Valid @RequestBody CreateProductRequest request) {

        CreateProductCommand command = new CreateProductCommand(
                request.data().attributes().nombre(),
                request.data().attributes().precio(),
                request.data().attributes().descripcion()
        );
        Product product = createProduct.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.parseMediaType(MEDIA_TYPE))
                .body(toResponse(product));
    }

    @GetMapping(value = "/{id}", produces = MEDIA_TYPE)
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<JsonApiResponse<ProductAttributes>> getById(@PathVariable UUID id) {
        Product product = getProduct.execute(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MEDIA_TYPE))
                .body(toResponse(product));
    }

    @GetMapping(produces = MEDIA_TYPE)
    @Operation(summary = "Listar todos los productos")
    public ResponseEntity<JsonApiListResponse<ProductAttributes>> listAll() {
        List<JsonApiData<ProductAttributes>> data = listProducts.execute().stream()
                .map(p -> new JsonApiData<>(TYPE, p.getId().toString(),
                        new ProductAttributes(p.getNombre(), p.getPrecio(), p.getDescripcion())))
                .collect(Collectors.toList());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MEDIA_TYPE))
                .body(new JsonApiListResponse<>(data));
    }

    private JsonApiResponse<ProductAttributes> toResponse(Product p) {
        return JsonApiResponse.of(TYPE, p.getId().toString(),
                new ProductAttributes(p.getNombre(), p.getPrecio(), p.getDescripcion()));
    }
}

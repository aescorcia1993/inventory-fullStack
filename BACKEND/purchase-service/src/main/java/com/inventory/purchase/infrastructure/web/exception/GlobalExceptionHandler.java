package com.inventory.purchase.infrastructure.web.exception;

import com.inventory.purchase.domain.exception.*;
import com.inventory.purchase.infrastructure.web.jsonapi.JsonApiError;
import com.inventory.purchase.infrastructure.web.jsonapi.JsonApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final MediaType JSON_API = MediaType.valueOf("application/vnd.api+json");

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<JsonApiErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(JSON_API)
                .body(JsonApiErrorResponse.single("404", "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<JsonApiErrorResponse> handleInventoryNotFound(InventoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(JSON_API)
                .body(JsonApiErrorResponse.single("404", "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<JsonApiErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).contentType(JSON_API)
                .body(JsonApiErrorResponse.single("422", "Insufficient Stock", ex.getMessage()));
    }

    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<JsonApiErrorResponse> handleServiceComm(ServiceCommunicationException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(JSON_API)
                .body(JsonApiErrorResponse.single("503", "Service Unavailable", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new JsonApiError("422", "Validation Error",
                        fe.getField() + ": " + fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).contentType(JSON_API)
                .body(new JsonApiErrorResponse(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonApiErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(JSON_API)
                .body(JsonApiErrorResponse.single("500", "Internal Server Error", "An unexpected error occurred"));
    }
}

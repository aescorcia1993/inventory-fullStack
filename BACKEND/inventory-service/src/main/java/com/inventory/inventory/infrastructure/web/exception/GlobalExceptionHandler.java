package com.inventory.inventory.infrastructure.web.exception;

import com.inventory.inventory.domain.exception.InventoryNotFoundException;
import com.inventory.inventory.domain.model.InsufficientStockException;
import com.inventory.inventory.infrastructure.web.jsonapi.JsonApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final MediaType JSON_API = MediaType.valueOf("application/vnd.api+json");

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<JsonApiErrorResponse> handleNotFound(InventoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(JSON_API)
                .body(JsonApiErrorResponse.single("404", "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<JsonApiErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).contentType(JSON_API)
                .body(JsonApiErrorResponse.single("422", "Insufficient Stock", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new com.inventory.inventory.infrastructure.web.jsonapi.JsonApiError(
                        "422", "Validation Error", fe.getField() + ": " + fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).contentType(JSON_API)
                .body(new JsonApiErrorResponse(errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JsonApiErrorResponse> handleIllegal(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).contentType(JSON_API)
                .body(JsonApiErrorResponse.single("422", "Invalid Argument", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonApiErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(JSON_API)
                .body(JsonApiErrorResponse.single("500", "Internal Server Error", "An unexpected error occurred"));
    }
}

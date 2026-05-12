package com.inventory.products.infrastructure.web.jsonapi;

import java.util.List;

public record JsonApiErrorResponse(List<JsonApiError> errors) {
    public static JsonApiErrorResponse single(String status, String title, String detail) {
        return new JsonApiErrorResponse(List.of(new JsonApiError(status, title, detail)));
    }
}

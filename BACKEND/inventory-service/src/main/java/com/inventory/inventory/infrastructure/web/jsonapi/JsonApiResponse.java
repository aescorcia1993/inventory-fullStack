package com.inventory.inventory.infrastructure.web.jsonapi;

public record JsonApiResponse<T>(JsonApiData<T> data) {
    public static <T> JsonApiResponse<T> of(String type, String id, T attributes) {
        return new JsonApiResponse<>(new JsonApiData<>(type, id, attributes));
    }
}

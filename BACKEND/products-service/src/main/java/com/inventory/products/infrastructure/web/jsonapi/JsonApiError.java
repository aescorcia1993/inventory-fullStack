package com.inventory.products.infrastructure.web.jsonapi;

public record JsonApiError(String status, String title, String detail) {}

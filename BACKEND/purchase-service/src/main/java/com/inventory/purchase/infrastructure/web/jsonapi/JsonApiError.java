package com.inventory.purchase.infrastructure.web.jsonapi;

public record JsonApiError(String status, String title, String detail) {}

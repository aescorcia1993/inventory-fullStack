package com.inventory.payment.infrastructure.web.jsonapi;

public record JsonApiData<T>(String type, String id, T attributes) {}

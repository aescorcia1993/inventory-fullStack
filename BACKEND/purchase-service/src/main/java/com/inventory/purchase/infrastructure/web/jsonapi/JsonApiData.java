package com.inventory.purchase.infrastructure.web.jsonapi;

public record JsonApiData<T>(String type, String id, T attributes) {}

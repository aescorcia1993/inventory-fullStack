package com.inventory.products.infrastructure.web.jsonapi;

import java.util.List;

public record JsonApiListResponse<T>(List<JsonApiData<T>> data) {}

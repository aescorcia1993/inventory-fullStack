package com.inventory.payment.infrastructure.web.jsonapi;

import java.util.List;

public record JsonApiListResponse<T>(List<JsonApiData<T>> data) {}

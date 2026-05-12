package com.inventory.inventory.infrastructure.web.jsonapi;

import java.util.List;

public record JsonApiError(String status, String title, String detail) {}

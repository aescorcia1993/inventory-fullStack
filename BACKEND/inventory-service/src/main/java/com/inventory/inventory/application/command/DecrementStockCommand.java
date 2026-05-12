package com.inventory.inventory.application.command;

import java.util.UUID;

public record DecrementStockCommand(UUID productoId, int amount) {}

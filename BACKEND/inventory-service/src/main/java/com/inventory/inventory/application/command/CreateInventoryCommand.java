package com.inventory.inventory.application.command;

import java.util.UUID;

public record CreateInventoryCommand(UUID productoId, int cantidad) {}

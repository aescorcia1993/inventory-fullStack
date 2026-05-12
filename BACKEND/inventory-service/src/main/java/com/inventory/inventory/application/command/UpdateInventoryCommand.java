package com.inventory.inventory.application.command;

import java.util.UUID;

public record UpdateInventoryCommand(UUID productoId, int cantidad) {}

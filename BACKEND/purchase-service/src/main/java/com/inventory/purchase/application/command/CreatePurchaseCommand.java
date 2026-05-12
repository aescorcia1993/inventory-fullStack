package com.inventory.purchase.application.command;

import java.util.UUID;

public record CreatePurchaseCommand(UUID productoId, int cantidad) {}

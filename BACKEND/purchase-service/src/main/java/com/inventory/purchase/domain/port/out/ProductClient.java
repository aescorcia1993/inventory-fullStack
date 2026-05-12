package com.inventory.purchase.domain.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductClient {
    BigDecimal getProductPrice(UUID productoId);
}

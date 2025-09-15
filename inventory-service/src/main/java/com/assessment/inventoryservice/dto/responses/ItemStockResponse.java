package com.assessment.inventoryservice.dto.responses;

import java.math.BigDecimal;

public record ItemStockResponse(String itemCode, String name, String description, int quantity, BigDecimal price) {
}

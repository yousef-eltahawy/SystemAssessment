package com.assessment.orderservice.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record LineItemRequest(
        @NotBlank(message = "Item code is required")
        String itemCode,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {}

package com.assessment.orderservice.dto.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "Customer Name is required")
        String customerName,

        @Valid
        @Size(min = 1, message = "At least one line item is required")
        List<LineItemRequest> lineItems
) {}

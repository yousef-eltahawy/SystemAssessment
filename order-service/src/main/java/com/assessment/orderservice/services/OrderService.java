package com.assessment.orderservice.services;

import com.assessment.orderservice.dto.requests.CreateOrderRequest;
import com.assessment.orderservice.dto.responses.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest createOrderRequest);

    OrderResponse getOrder(String orderId);
}

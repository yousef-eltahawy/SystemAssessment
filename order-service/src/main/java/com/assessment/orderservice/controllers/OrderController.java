package com.assessment.orderservice.controllers;

import com.assessment.orderservice.dto.requests.CreateOrderRequest;
import com.assessment.orderservice.dto.responses.OrderResponse;
import com.assessment.orderservice.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(final OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest){
        OrderResponse orderResponse = orderService.createOrder(createOrderRequest);
        return ResponseEntity.ok(orderResponse);
    }
    @GetMapping("/get/{orderCode}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderCode){
        OrderResponse orderResponse = orderService.getOrder(orderCode);
        return ResponseEntity.ok(orderResponse);
    }

}

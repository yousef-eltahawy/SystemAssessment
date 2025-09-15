package com.assessment.orderservice.servicesImpl;


import com.assessment.orderservice.dto.requests.CreateOrderRequest;
import com.assessment.orderservice.dto.requests.LineItemRequest;
import com.assessment.orderservice.dto.responses.OrderResponse;
import com.assessment.orderservice.entities.Order;
import com.assessment.orderservice.exceptions.ItemNotFoundException;
import com.assessment.orderservice.exceptions.NotEnoughQuantityException;
import com.assessment.orderservice.exceptions.OrderNotFoundException;
import com.assessment.orderservice.grpc.*;
import com.assessment.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemStockServiceGrpc.ItemStockServiceBlockingStub stub;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        orderService = new OrderServiceImpl(modelMapper, orderRepository);
        // Inject the mocked stub manually
        orderService.setStub(stub);
    }

    @Test
    void createOrder_success() {
        // Arrange
        LineItemRequest lineItemRequest = new LineItemRequest("ITEM-1001", 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", List.of(lineItemRequest));

        ConsumeStockResponse consumeStockResponse = ConsumeStockResponse.newBuilder()
                .setAvailable(true)
                .setItemCode("ITEM-1001")
                .setItemName("Laptop")
                .setRequiredQty(2)
                .setPrice("1000")
                .build();

        ConsumeStockListResponse consumeStockListResponse = ConsumeStockListResponse.newBuilder()
                .addResponses(consumeStockResponse)
                .build();

        when(stub.consumeStock(any(ConsumeStockListRequest.class))).thenReturn(consumeStockListResponse);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals("John Doe", response.getCustomerName());
        assertEquals(2000, response.getNetPrice().intValue());
        assertEquals(14, response.getTaxPercentage());
        assertEquals(7, response.getVatPercentage());
        assertEquals(2000 + 280 + 140, response.getGrossPrice().intValue());
        assertEquals(1, response.getLineItems().size());

        verify(stub, times(1)).consumeStock(any());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_itemNotFound_throwsException() {
        LineItemRequest lineItemRequest = new LineItemRequest("ITEM-9999", 1);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", List.of(lineItemRequest));

        ConsumeStockResponse consumeStockResponse = ConsumeStockResponse.newBuilder()
                .setAvailable(false)
                .setAvailableQty(-1)
                .setItemCode("ITEM-9999")
                .build();

        ConsumeStockListResponse consumeStockListResponse = ConsumeStockListResponse.newBuilder()
                .addResponses(consumeStockResponse)
                .build();

        when(stub.consumeStock(any())).thenReturn(consumeStockListResponse);

        assertThrows(ItemNotFoundException.class, () -> orderService.createOrder(request));
    }

    @Test
    void createOrder_notEnoughQuantity_throwsException() {
        LineItemRequest lineItemRequest = new LineItemRequest("ITEM-1001", 5);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", List.of(lineItemRequest));

        ConsumeStockResponse consumeStockResponse = ConsumeStockResponse.newBuilder()
                .setAvailable(false)
                .setAvailableQty(2)
                .setItemCode("ITEM-1001")
                .build();

        ConsumeStockListResponse consumeStockListResponse = ConsumeStockListResponse.newBuilder()
                .addResponses(consumeStockResponse)
                .build();

        when(stub.consumeStock(any())).thenReturn(consumeStockListResponse);

        assertThrows(NotEnoughQuantityException.class, () -> orderService.createOrder(request));
    }

    @Test
    void getOrder_success() {
        Order order = new Order();
        order.setCode("ORDER-123");
        order.setCustomerName("John Doe");

        when(orderRepository.findByCode("ORDER-123")).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrder("ORDER-123");

        assertNotNull(response);
        assertEquals("John Doe", response.getCustomerName());
    }

    @Test
    void getOrder_notFound_throwsException() {
        when(orderRepository.findByCode(anyString())).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder("ORDER-999"));
    }
}

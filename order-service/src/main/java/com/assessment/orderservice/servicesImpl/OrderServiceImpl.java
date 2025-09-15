package com.assessment.orderservice.servicesImpl;

import com.assessment.orderservice.dto.requests.CreateOrderRequest;
import com.assessment.orderservice.dto.requests.LineItemRequest;
import com.assessment.orderservice.dto.responses.OrderResponse;
import com.assessment.orderservice.entities.LineItem;
import com.assessment.orderservice.entities.Order;
import com.assessment.orderservice.exceptions.ItemNotFoundException;
import com.assessment.orderservice.exceptions.NotEnoughQuantityException;
import com.assessment.orderservice.exceptions.OrderNotFoundException;
import com.assessment.orderservice.grpc.*;
import com.assessment.orderservice.repository.OrderRepository;
import com.assessment.orderservice.services.OrderService;
import com.google.common.annotations.VisibleForTesting;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final ModelMapper modelMapper ;
    private final OrderRepository orderRepository;
    @VisibleForTesting
    @GrpcClient("itemStockService")
    private ItemStockServiceGrpc.ItemStockServiceBlockingStub stub ;

    @Autowired
    public OrderServiceImpl(ModelMapper modelMapper, OrderRepository orderRepository) {
        this.modelMapper = modelMapper;
        this.orderRepository = orderRepository;
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        ConsumeStockListRequest consumeRequest = createConsumeStockListRequest(request.lineItems());

        ConsumeStockListResponse response = stub.consumeStock(consumeRequest);

        List<LineItem> lineItems = createOrderLineItems(response,order);

        order.setCode("ORDER-"+generateUniqueCode());
        order.setCustomerName(request.customerName());
        order.setTaxPercentage(14.0d);
        order.setVatPercentage(7d);
        order.setLineItems(lineItems);

        calculateCharges(order);

        order = orderRepository.save(order);

        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    public OrderResponse getOrder(String orderCode) {
        Optional<Order> optOrder = orderRepository.findByCode(orderCode);
        return optOrder.map(order -> modelMapper.map(order, OrderResponse.class))
                .orElseThrow(() -> new OrderNotFoundException("No Order Found with Code ["+orderCode+"]"));
    }

    private ConsumeStockListRequest createConsumeStockListRequest(List<LineItemRequest> lineItems) {
        ConsumeStockListRequest.Builder builder = ConsumeStockListRequest.newBuilder();
        for (LineItemRequest lineItem : lineItems) {
            builder.addRequests(ConsumeStockRequest.newBuilder()
                    .setItemCode(lineItem.itemCode())
                    .setQuantity(lineItem.quantity())
                    .build());
        }
        return builder.build();
    }
    private List<LineItem> createOrderLineItems(ConsumeStockListResponse responses, Order order) {
        List<LineItem> lineItems = new ArrayList<>();
        for(ConsumeStockResponse response : responses.getResponsesList()){
            if(response.getAvailable()){
                LineItem lineItem = new LineItem();
                lineItem.setCode("LineItem-"+generateUniqueCode());
                lineItem.setItemCode(response.getItemCode());
                lineItem.setItemName(response.getItemName());
                lineItem.setQuantity(response.getRequiredQty());
                lineItem.setUnitPrice(new BigDecimal(response.getPrice()));
                lineItem.setTotalPrice(lineItem.getUnitPrice().multiply(BigDecimal.valueOf(lineItem.getQuantity())));
                lineItem.setOrder(order);
                lineItems.add(lineItem);
            }else{
                if(response.getAvailableQty() == -1){
                    throw new ItemNotFoundException("No Item Found with This Code");
                }else{
                    throw new NotEnoughQuantityException("The only available Quantity for Item Code ["+response.getItemCode()+"] is ["+response.getAvailableQty()+"]");
                }
            }
        }
        return lineItems;
    }

    private void calculateCharges(Order order) {
        BigDecimal netPrice = order.getLineItems().stream()
                .map(LineItem::getTotalPrice)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        order.setNetPrice(netPrice);

        BigDecimal taxAmount = calculatePercentage(netPrice, order.getTaxPercentage());
        BigDecimal vatAmount = calculatePercentage(netPrice, order.getVatPercentage());

        order.setTaxAmount(taxAmount);
        order.setVatAmount(vatAmount);
        order.setGrossPrice(netPrice.add(taxAmount).add(vatAmount));
    }

    private BigDecimal calculatePercentage(BigDecimal base, double percentage) {
        return base.multiply(
                BigDecimal.valueOf(percentage)
                        .divide(BigDecimal.valueOf(100),4, RoundingMode.HALF_UP));
    }
    private static String generateUniqueCode() {
        int length = 10;
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return length <= uuid.length() ? uuid.substring(0, length) : uuid;
    }
    @VisibleForTesting
    public void setStub(ItemStockServiceGrpc.ItemStockServiceBlockingStub stub) {
        this.stub = stub;
    }
}

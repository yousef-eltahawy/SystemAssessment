package com.assessment.inventoryservice.servicesImpl;

import com.assessment.inventoryservice.dto.responses.ItemStockResponse;
import com.assessment.inventoryservice.entities.Item;
import com.assessment.inventoryservice.exceptions.ItemNotFoundException;
import com.assessment.inventoryservice.grpc.*;

import com.assessment.inventoryservice.repository.ItemRepository;
import com.assessment.inventoryservice.services.ItemService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.Optional;

@GrpcService
public class ItemServiceImpl extends ItemStockServiceGrpc.ItemStockServiceImplBase implements ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;

    }

    @Override
    public void consumeStock(ConsumeStockListRequest request, StreamObserver<ConsumeStockListResponse> responseObserver) {
        ConsumeStockListResponse.Builder responseBuilder = ConsumeStockListResponse.newBuilder();
        for(ConsumeStockRequest r : request.getRequestsList()){
            ConsumeStockResponse response ;
            Optional<Item> optItem = itemRepository.findByCode(r.getItemCode());
            if (!optItem.isPresent()) {
                response = ConsumeStockResponse.newBuilder()
                        .setAvailable(false)
                        .setAvailableQty(-1)
                        .build();
            }else {
                Item item = optItem.get();
                if(item.getQuantity() < r.getQuantity()) {
                    response = ConsumeStockResponse.newBuilder()
                            .setAvailable(false)
                            .setAvailableQty(item.getQuantity())
                            .setItemCode(item.getCode()).build();
                }
                else {
                    response = ConsumeStockResponse.newBuilder()
                            .setAvailable(true)
                            .setAvailableQty(item.getQuantity())
                            .setRequiredQty(r.getQuantity())
                            .setItemCode(item.getCode())
                            .setItemName(item.getName())
                            .setPrice(String.valueOf(item.getPrice())).build();
                    item.setQuantity(item.getQuantity()-r.getQuantity());
                    itemRepository.save(item);
                }
            }
            responseBuilder.addResponses(response);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public ItemStockResponse getItemStock(String itemCode) {
        return itemRepository.findByCode(itemCode)
                .map(item -> new ItemStockResponse(
                        item.getCode()
                        ,item.getName()
                        ,item.getDescription()
                        ,item.getQuantity()
                        ,item.getPrice()))
                .orElseThrow(() -> new ItemNotFoundException("No Item found with code [" + itemCode+"]"));
    }
}

package com.assessment.inventoryservice.servicesImpl;

import com.assessment.inventoryservice.dto.responses.ItemStockResponse;
import com.assessment.inventoryservice.entities.Item;
import com.assessment.inventoryservice.exceptions.ItemNotFoundException;
import com.assessment.inventoryservice.grpc.*;
import com.assessment.inventoryservice.repository.ItemRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;


    // ===========================
    // Tests for getItemStock
    // ===========================
    @Test
    void testGetItemStock_ItemExists() {
        Item item = new Item();
        item.setCode("ITEM-1001");
        item.setName("Laptop");
        item.setDescription("Gaming Laptop");
        item.setQuantity(10);
        item.setPrice(new BigDecimal("1500.50"));

        when(itemRepository.findByCode(anyString())).thenReturn(Optional.of(item));

        ItemStockResponse response = itemService.getItemStock("ITEM-1001");

        assertNotNull(response);
        assertEquals("ITEM-1001", response.itemCode());
        assertEquals("Laptop", response.name());
        assertEquals("Gaming Laptop", response.description());
        assertEquals(10, response.quantity());
        assertEquals(new BigDecimal("1500.50"), response.price());
    }

    @Test
    void testGetItemStock_ItemNotFound() {
        when(itemRepository.findByCode("ITEM-1002")).thenReturn(Optional.empty());

        ItemNotFoundException ex = assertThrows(ItemNotFoundException.class, () ->
                itemService.getItemStock("ITEM-1002"));

        assertEquals("No Item found with code [ITEM-1002]", ex.getMessage());
    }

    // ===========================
    // Tests for consumeStock
    // ===========================
    @Test
    void testConsumeStock_ItemAvailable() {
        Item item = new Item();
        item.setCode("ITEM-1001");
        item.setName("Laptop");
        item.setQuantity(10);
        item.setPrice(new BigDecimal("1500.50"));

        when(itemRepository.findByCode(anyString())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);


        ConsumeStockRequest requestProto = ConsumeStockRequest.newBuilder()
                .setItemCode("ITEM-1001")
                .setQuantity(5)
                .build();
        ConsumeStockListRequest listRequest = ConsumeStockListRequest.newBuilder()
                .addRequests(requestProto)
                .build();

        @SuppressWarnings("unchecked")
        StreamObserver<ConsumeStockListResponse> responseObserver = mock(StreamObserver.class);

        itemService.consumeStock(listRequest, responseObserver);

        // verify StreamObserver called once
        ArgumentCaptor<ConsumeStockListResponse> captor = ArgumentCaptor.forClass(ConsumeStockListResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        ConsumeStockListResponse response = captor.getValue();
        assertEquals(1, response.getResponsesCount());
        ConsumeStockResponse r = response.getResponses(0);
        assertTrue(r.getAvailable());
        assertEquals(10, r.getAvailableQty());
        assertEquals(5, r.getRequiredQty());
        assertEquals("ITEM-1001", r.getItemCode());
        assertEquals("Laptop", r.getItemName());
        assertEquals("1500.50", r.getPrice());
    }

    @Test
    void testConsumeStock_ItemNotEnoughQuantity() {
        Item item = new Item();
        item.setCode("ITEM-1001");
        item.setQuantity(3);

        when(itemRepository.findByCode("ITEM-1001")).thenReturn(Optional.of(item));

        ConsumeStockRequest requestProto = ConsumeStockRequest.newBuilder()
                .setItemCode("ITEM-1001")
                .setQuantity(5)
                .build();
        ConsumeStockListRequest listRequest = ConsumeStockListRequest.newBuilder()
                .addRequests(requestProto)
                .build();

        @SuppressWarnings("unchecked")
        StreamObserver<ConsumeStockListResponse> responseObserver = mock(StreamObserver.class);

        itemService.consumeStock(listRequest, responseObserver);

        ArgumentCaptor<ConsumeStockListResponse> captor = ArgumentCaptor.forClass(ConsumeStockListResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        ConsumeStockListResponse response = captor.getValue();
        ConsumeStockResponse r = response.getResponses(0);
        assertFalse(r.getAvailable());
        assertNotEquals(5, r.getAvailableQty());
        assertEquals("ITEM-1001", r.getItemCode());
    }

    @Test
    void testConsumeStock_ItemNotFound() {
        when(itemRepository.findByCode("ITEM-9999")).thenReturn(Optional.empty());

        ConsumeStockRequest requestProto = ConsumeStockRequest.newBuilder()
                .setItemCode("ITEM-9999")
                .setQuantity(5)
                .build();
        ConsumeStockListRequest listRequest = ConsumeStockListRequest.newBuilder()
                .addRequests(requestProto)
                .build();

        @SuppressWarnings("unchecked")
        StreamObserver<ConsumeStockListResponse> responseObserver = mock(StreamObserver.class);

        itemService.consumeStock(listRequest, responseObserver);

        ArgumentCaptor<ConsumeStockListResponse> captor = ArgumentCaptor.forClass(ConsumeStockListResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        ConsumeStockListResponse response = captor.getValue();
        ConsumeStockResponse r = response.getResponses(0);
        assertFalse(r.getAvailable());
        assertEquals(-1, r.getAvailableQty());
    }
}
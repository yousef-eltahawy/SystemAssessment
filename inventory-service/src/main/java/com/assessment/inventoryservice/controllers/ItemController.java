package com.assessment.inventoryservice.controllers;

import com.assessment.inventoryservice.dto.responses.ItemStockResponse;
import com.assessment.inventoryservice.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
@RestController
@RequestMapping("api/v1/item")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(final ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("item/stock/{itemCode}")
    public ResponseEntity<ItemStockResponse> getItemStock(@PathVariable String itemCode){
        ItemStockResponse itemStockResponse = itemService.getItemStock(itemCode);
        return ResponseEntity.ok(itemStockResponse);
    }

}

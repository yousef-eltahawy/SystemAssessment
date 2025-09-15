package com.assessment.inventoryservice.services;

import com.assessment.inventoryservice.dto.responses.ItemStockResponse;

public interface ItemService {

    ItemStockResponse getItemStock(String itemCode);
}

package com.assessment.orderservice.dto.responses;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class OrderResponse extends BaseDto{

    private String CustomerName;
    private BigDecimal netPrice;
    private Double taxPercentage;
    private BigDecimal taxAmount;
    private Double vatPercentage;
    private BigDecimal vatAmount;
    private BigDecimal grossPrice;
    private List<LineItemResponse> lineItems = new ArrayList<>();
}

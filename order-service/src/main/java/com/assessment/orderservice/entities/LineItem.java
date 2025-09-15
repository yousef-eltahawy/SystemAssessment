package com.assessment.orderservice.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Table(name = "LINE_ITEM")
public class LineItem extends BaseEntity{

    @Column(name = "ITEM_CODE",nullable = false)
    private String ItemCode;
    @Column(name = "ITEM_NAME")
    private String ItemName;
    @Column(name = "QUANTITY",nullable = false)
    private int Quantity;
    @Column(name = "UNIT_PRICE", nullable = false, precision = 19, scale = 4,columnDefinition = "DECIMAL(19,4) default 0.0")
    private BigDecimal UnitPrice;
    @Column(name = "TOTAL_PRICE", nullable = false, precision = 19, scale = 4,columnDefinition = "DECIMAL(19,4) default 0.0")
    private BigDecimal TotalPrice;
    @ManyToOne
    @JoinColumn(name = "ORDER_ID",nullable = false)
    Order order;

}

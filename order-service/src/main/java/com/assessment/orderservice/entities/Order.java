package com.assessment.orderservice.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "ORDERS")
public class Order extends BaseEntity{

    @Column(name = "CUSTOMER_NAME",nullable = false)
    private String CustomerName;
    @Column(name = "NET_PRICE", nullable = false, precision = 19, scale = 4,columnDefinition = "DECIMAL(19,4) default 0.0")
    private BigDecimal netPrice;
    @Column(name = "TAX_PERCENTAGE")
    private Double taxPercentage;
    @Column(name = "TAX_AMOUNT", precision = 19, scale = 4,columnDefinition = "DECIMAL(19,4) default 0.0")
    private BigDecimal taxAmount;
    @Column(name = "VAT_PERCENTAGE")
    private Double vatPercentage;
    @Column(name = "VAT_AMOUNT", precision = 19, scale = 4,columnDefinition = "DECIMAL(19,4) default 0.0")
    private BigDecimal vatAmount;
    @Column(name = "GROSS_PRICE", nullable = false, precision = 19, scale = 4,columnDefinition = "DECIMAL(19,4) default 0.0")
    private BigDecimal grossPrice;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineItem> lineItems = new ArrayList<>();
}

package com.cosmocats.market.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class Order {

    private Long id;
    private List<Product> products;
    private BigDecimal totalPrice;
    private OrderStatus status;

    public Order() {
    }

    public Order(Long id, List<Product> products, BigDecimal totalPrice, OrderStatus status) {
        this.id = id;
        this.products = products;
        this.totalPrice = totalPrice;
        this.status = status;
    }

}
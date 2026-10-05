package com.cosmocats.market.domain;

import java.math.BigDecimal;
import java.util.List;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
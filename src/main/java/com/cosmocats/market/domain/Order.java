package com.cosmocats.market.domain;

import java.util.List;

public class Order {

    private Long id;
    private List<Product> products;
    private Double totalPrice;
    private String status;

    public Order() {
    }

    public Order(Long id, List<Product> products, Double totalPrice, String status) {
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

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
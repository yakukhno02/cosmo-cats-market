package com.cosmocats.market.domain;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    private Long id;
    private List<Product> products = new ArrayList<>();

    public Cart() {
    }

    public Cart(Long id, List<Product> products) {
        this.id = id;
        this.products = products;
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
}
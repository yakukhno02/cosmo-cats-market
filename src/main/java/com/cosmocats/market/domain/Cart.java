package com.cosmocats.market.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Cart {

    private Long id;
    private List<Product> products = new ArrayList<>();

    public Cart() {
    }

    public Cart(Long id, List<Product> products) {
        this.id = id;
        this.products = products;
    }

}
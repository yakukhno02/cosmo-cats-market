package com.cosmocats.market.domain;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Category {

    private Long id;
    private String name;
    private String description;

    public Category() {
    }

    public Category(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

}
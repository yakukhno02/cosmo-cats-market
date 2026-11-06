package com.cosmocats.market.repository.projection;

public interface TopSellingProductProjection {

    Long getProductId();

    String getProductName();

    Long getTotalQuantitySold();
}

package com.cosmocats.market.service;

import com.cosmocats.market.domain.Order;
import com.cosmocats.market.repository.projection.TopSellingProductProjection;

import java.util.List;

public interface OrderService {

    Order placeOrder(String customerEmail, List<Long> productIds);

    Order getOrderByNaturalId(String orderNumber);

    void cancelOrder(String orderNumber);

    List<TopSellingProductProjection> getTopSellingProductsReport();
}

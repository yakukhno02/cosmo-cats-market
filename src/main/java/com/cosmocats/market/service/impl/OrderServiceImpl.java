package com.cosmocats.market.service.impl;

import com.cosmocats.market.domain.Order;
import com.cosmocats.market.domain.OrderStatus;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.exception.ProductNotFoundException;
import com.cosmocats.market.repository.OrderRepository;
import com.cosmocats.market.repository.ProductRepository;
import com.cosmocats.market.repository.entity.OrderEntity;
import com.cosmocats.market.repository.entity.OrderItemEntity;
import com.cosmocats.market.repository.entity.ProductEntity;
import com.cosmocats.market.repository.projection.TopSellingProductProjection;
import com.cosmocats.market.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Order placeOrder(String customerEmail, List<Long> productIds) {
        String naturalId = "ORD-" + UUID.randomUUID();

        OrderEntity orderEntity = OrderEntity.builder()
                .orderNumber(naturalId)
                .customerEmail(customerEmail)
                .status(OrderStatus.CREATED.name())
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<Product> domainProducts = new ArrayList<>();

        for (Long productId : productIds) {
            ProductEntity productEntity = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            OrderItemEntity item = OrderItemEntity.builder()
                    .order(orderEntity)
                    .product(productEntity)
                    .quantity(1)
                    .price(productEntity.getPrice())
                    .build();

            orderEntity.getItems().add(item);
            total = total.add(productEntity.getPrice());

            domainProducts.add(new Product(
                    productEntity.getId(),
                    productEntity.getName(),
                    productEntity.getDescription(),
                    productEntity.getPrice(),
                    null
            ));
        }

        orderEntity.setTotalPrice(total);
        OrderEntity saved = orderRepository.save(orderEntity);

        return new Order(saved.getId(), domainProducts, saved.getTotalPrice(), OrderStatus.CREATED);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByNaturalId(String orderNumber) {
        OrderEntity orderEntity = orderRepository.findByNaturalId(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNumber));

        List<Product> domainProducts = orderEntity.getItems().stream()
                .map(item -> new Product(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getDescription(),
                        item.getPrice(),
                        null
                ))
                .toList();

        return new Order(orderEntity.getId(), domainProducts, orderEntity.getTotalPrice(), OrderStatus.valueOf(orderEntity.getStatus()));
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNumber) {
        OrderEntity orderEntity = orderRepository.findByNaturalId(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNumber));
        orderEntity.setStatus(OrderStatus.CANCELLED.name());
        orderRepository.save(orderEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopSellingProductProjection> getTopSellingProductsReport() {
        return orderRepository.findTopSellingProducts();
    }
}

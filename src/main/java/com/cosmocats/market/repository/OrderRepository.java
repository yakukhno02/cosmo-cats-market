package com.cosmocats.market.repository;

import com.cosmocats.market.repository.entity.OrderEntity;
import com.cosmocats.market.repository.projection.TopSellingProductProjection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends NaturalIdRepository<OrderEntity, String> {

    @Query("""
        SELECT oi.product.id AS productId,
               oi.product.name AS productName,
               SUM(oi.quantity) AS totalQuantitySold
        FROM OrderEntity o
        JOIN o.items oi
        WHERE o.status != 'CANCELLED'
        GROUP BY oi.product.id, oi.product.name
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<TopSellingProductProjection> findTopSellingProducts();
}

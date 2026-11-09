package com.cosmocats.market.repository;

import com.cosmocats.market.repository.entity.ProductEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findByNameAndCategoryId(String name, Long categoryId);

    List<ProductEntity> findByCategoryId(Long categoryId);
}

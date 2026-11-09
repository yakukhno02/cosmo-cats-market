package com.cosmocats.market.mapper;

import com.cosmocats.market.domain.Category;
import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequestDto;
import com.cosmocats.market.dto.ProductResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toDomain(ProductRequestDto dto) {
        Category category = new Category(null, dto.getCategory(), null);

        return new Product(
                null,
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                category);
    }

    public ProductResponseDto toDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().getName()
        );
    }
}
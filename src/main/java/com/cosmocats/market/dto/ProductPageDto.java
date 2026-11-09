package com.cosmocats.market.dto;

import java.util.List;

public record ProductPageDto(
        List<ProductResponseDto> content,
        int page,
        int size,
        long totalElements
) {}

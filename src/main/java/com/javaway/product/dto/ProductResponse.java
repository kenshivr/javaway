package com.javaway.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        CategoryResponse category,
        List<ProductImageResponse> images,
        LocalDateTime createdAt
) {}

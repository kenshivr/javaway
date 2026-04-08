package com.javaway.product.dto;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Long parentId
) {}

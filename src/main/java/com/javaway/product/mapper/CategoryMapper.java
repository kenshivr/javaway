package com.javaway.product.mapper;

import com.javaway.product.dto.CategoryResponse;
import com.javaway.product.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    CategoryResponse toResponse(Category category);
}

package com.javaway.product.mapper;

import com.javaway.product.dto.ProductImageResponse;
import com.javaway.product.dto.ProductResponse;
import com.javaway.product.model.Product;
import com.javaway.product.model.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    @Mapping(target = "isPrimary", expression = "java(image.isPrimary())")
    ProductImageResponse toImageResponse(ProductImage image);
}

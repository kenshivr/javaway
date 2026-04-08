package com.javaway.order.mapper;

import com.javaway.order.dto.OrderItemResponse;
import com.javaway.order.dto.OrderResponse;
import com.javaway.order.model.Order;
import com.javaway.order.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderItemResponse toItemResponse(OrderItem item);
}

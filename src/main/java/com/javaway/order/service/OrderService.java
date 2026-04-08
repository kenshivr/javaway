package com.javaway.order.service;

import com.javaway.order.dto.CreateOrderRequest;
import com.javaway.order.dto.OrderResponse;
import com.javaway.order.dto.UpdateOrderStatusRequest;
import com.javaway.order.mapper.OrderMapper;
import com.javaway.order.model.Order;
import com.javaway.order.model.OrderItem;
import com.javaway.order.repository.OrderRepository;
import com.javaway.product.model.Product;
import com.javaway.product.repository.ProductRepository;
import com.javaway.shared.enums.OrderStatus;
import com.javaway.shared.util.SecurityUtils;
import com.javaway.user.model.Address;
import com.javaway.user.model.User;
import com.javaway.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper orderMapper;
    private final SecurityUtils securityUtils;

    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        Long userId = securityUtils.getCurrentUser().getId();
        return orderRepository.findByUserId(userId, pageable).map(orderMapper::toResponse);
    }

    public OrderResponse getById(Long id) {
        Long userId = securityUtils.getCurrentUser().getId();
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return orderMapper.toResponse(order);
    }

    public Page<OrderResponse> getAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toResponse);
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        User user = securityUtils.getCurrentUser();

        Address address = addressRepository.findByIdAndUserId(request.shippingAddressId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemReq.productId()));

            if (product.getStock() < itemReq.quantity()) {
                throw new IllegalArgumentException("Insufficient stock for: " + product.getName());
            }

            product.setStock(product.getStock() - itemReq.quantity());
            productRepository.save(product);

            items.add(OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .priceAtPurchase(product.getPrice())
                    .build());

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())));
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .total(total)
                .shippingAddress(address)
                .items(items)
                .build();

        items.forEach(item -> item.setOrder(order));
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(request.status());
        return orderMapper.toResponse(orderRepository.save(order));
    }
}

package com.javaway.order.service;

import com.javaway.order.dto.CreateOrderRequest;
import com.javaway.order.dto.OrderItemRequest;
import com.javaway.order.dto.OrderResponse;
import com.javaway.order.dto.UpdateOrderStatusRequest;
import com.javaway.order.mapper.OrderMapper;
import com.javaway.order.model.Order;
import com.javaway.order.repository.OrderRepository;
import com.javaway.product.model.Product;
import com.javaway.product.repository.ProductRepository;
import com.javaway.shared.enums.OrderStatus;
import com.javaway.shared.enums.Role;
import com.javaway.shared.util.SecurityUtils;
import com.javaway.user.model.Address;
import com.javaway.user.model.User;
import com.javaway.user.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock AddressRepository addressRepository;
    @Mock OrderMapper orderMapper;
    @Mock SecurityUtils securityUtils;

    @InjectMocks OrderService orderService;

    private User user;
    private Address address;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@email.com").role(Role.CUSTOMER).build();
        address = Address.builder().id(1L).user(user).street("Calle 123").city("CABA")
                .state("Buenos Aires").zipCode("1000").country("Argentina").build();
        product = Product.builder().id(1L).name("Laptop").price(new BigDecimal("999.99")).stock(10).build();
    }

    @Test
    void createOrder_success() {
        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(new OrderItemRequest(1L, 2)));
        Order savedOrder = Order.builder().id(1L).user(user).status(OrderStatus.PENDING)
                .total(new BigDecimal("1999.98")).items(List.of()).build();
        OrderResponse expected = new OrderResponse(1L, OrderStatus.PENDING, new BigDecimal("1999.98"), List.of(), null);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenReturn(savedOrder);
        when(orderMapper.toResponse(savedOrder)).thenReturn(expected);

        OrderResponse response = orderService.create(request);

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.total()).isEqualByComparingTo("1999.98");
        assertThat(product.getStock()).isEqualTo(8);
        verify(productRepository).save(product);
    }

    @Test
    void createOrder_insufficientStock_throwsException() {
        product.setStock(1);
        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(new OrderItemRequest(1L, 5)));

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock for: Laptop");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_productNotFound_throwsException() {
        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(new OrderItemRequest(99L, 1)));

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found: 99");
    }

    @Test
    void createOrder_addressNotFound_throwsException() {
        CreateOrderRequest request = new CreateOrderRequest(99L, List.of(new OrderItemRequest(1L, 1)));

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Address not found");
    }

    @Test
    void updateStatus_success() {
        Order order = Order.builder().id(1L).status(OrderStatus.PENDING).items(List.of()).build();
        OrderResponse expected = new OrderResponse(1L, OrderStatus.CONFIRMED, BigDecimal.ZERO, List.of(), null);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse response = orderService.updateStatus(1L, new UpdateOrderStatusRequest(OrderStatus.CONFIRMED));

        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }
}

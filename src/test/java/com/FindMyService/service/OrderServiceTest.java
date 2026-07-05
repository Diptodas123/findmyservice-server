package com.FindMyService.service;

import com.FindMyService.model.Order;
import com.FindMyService.model.Provider;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.User;
import com.FindMyService.model.dto.OrderDto;
import com.FindMyService.model.enums.OrderStatus;
import com.FindMyService.repository.OrderRepository;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import com.FindMyService.repository.UserRepository;
import com.FindMyService.utils.OwnerCheck;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProviderRepository providerRepository;
    @Mock private ServiceCatalogRepository serviceCatalogRepository;
    @Mock private OwnerCheck ownerCheck;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Provider testProvider;
    private ServiceCatalog testService;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);

        testProvider = new Provider();
        testProvider.setProviderId(1L);

        testService = new ServiceCatalog();
        testService.setServiceId(1L);
        testService.setCost(new BigDecimal("50.00"));

        testOrder = Order.builder()
                .orderId(1L)
                .userId(testUser)
                .providerId(testProvider)
                .serviceId(testService)
                .orderStatus(OrderStatus.REQUESTED)
                .quantity(1)
                .totalCost(new BigDecimal("50.00"))
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllOrdersReturnsMappedDtos() {
        when(orderRepository.findAllWithRefs()).thenReturn(List.of(testOrder));
        List<OrderDto> result = orderService.getAllOrders();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(1L);
    }

    @Test
    void getOrderByIdFound() {
        when(orderRepository.findByIdWithRefs(1L)).thenReturn(Optional.of(testOrder));
        assertThat(orderService.getOrderById(1L)).isPresent();
    }

    @Test
    void getOrderByIdNotFound() {
        when(orderRepository.findByIdWithRefs(99L)).thenReturn(Optional.empty());
        assertThat(orderService.getOrderById(99L)).isEmpty();
    }

    @Test
    void createOrderSavesAndReturnsDto() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        dto.setProviderId(1L);
        dto.setServiceId(1L);
        dto.setQuantity(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        when(serviceCatalogRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(orderRepository.save(any())).thenReturn(testOrder);

        OrderDto result = orderService.createOrder(dto);

        assertThat(result).isNotNull();
        verify(orderRepository).save(any());
    }

    @Test
    void createOrderUserNotFoundThrows() {
        OrderDto dto = new OrderDto();
        dto.setUserId(99L);
        dto.setProviderId(1L);
        dto.setServiceId(1L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createOrdersBatchCreatesOnePerItem() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        dto.setProviderId(1L);
        dto.setServiceId(1L);
        dto.setQuantity(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        when(serviceCatalogRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(orderRepository.save(any())).thenReturn(testOrder);

        List<OrderDto> result = orderService.createOrdersBatch(List.of(dto, dto));

        assertThat(result).hasSize(2);
        verify(orderRepository, times(2)).save(any());
    }

    @Test
    void deleteOrderReturnsTrueWhenFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        assertThat(orderService.deleteOrder(1L)).isTrue();
        verify(orderRepository).delete(testOrder);
    }

    @Test
    void deleteOrderReturnsFalseWhenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(orderService.deleteOrder(99L)).isFalse();
        verify(orderRepository, never()).delete(any());
    }

    @Test
    void getOrdersByUserNotFoundReturnsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = orderService.getOrdersByUser(99L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getOrdersByUserReturnsOk() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(testUser)).thenReturn(List.of(testOrder));
        ResponseEntity<?> response = orderService.getOrdersByUser(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getOrdersByProviderNotFoundReturnsNotFound() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = orderService.getOrdersByProvider(99L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getOrdersByProviderReturnsOk() {
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        when(orderRepository.findByProviderId(testProvider)).thenReturn(List.of(testOrder));
        ResponseEntity<?> response = orderService.getOrdersByProvider(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateOrderAsProviderSetsScheduledDate() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("p@test.com", null, "PROVIDER"));

        OrderDto patch = new OrderDto();
        patch.setOrderStatus(OrderStatus.SCHEDULED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);
        when(orderRepository.findByIdWithRefs(1L)).thenReturn(Optional.of(testOrder));

        OrderDto result = orderService.updateOrder(1L, patch);

        assertThat(result).isNotNull();
        assertThat(testOrder.getOrderStatus()).isEqualTo(OrderStatus.SCHEDULED);
    }

    @Test
    void updateOrderAsUserCanCancel() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("u@test.com", null, "USER"));

        OrderDto patch = new OrderDto();
        patch.setOrderStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);
        when(orderRepository.findByIdWithRefs(1L)).thenReturn(Optional.of(testOrder));

        orderService.updateOrder(1L, patch);

        assertThat(testOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void updateOrderAsUserCannotSetScheduledDate() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("u@test.com", null, "USER"));

        OrderDto patch = new OrderDto();
        patch.setScheduledDate(java.time.Instant.now());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.updateOrder(1L, patch))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Users cannot modify scheduledDate");
    }

    @Test
    void updateCompletedOrderThrows() {
        testOrder.setOrderStatus(OrderStatus.COMPLETED);
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("u@test.com", null, "USER"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.updateOrder(1L, new OrderDto()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot update an order with status");
    }

    @Test
    void updateOrderAsAdminUpdatesAllFields() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin@test.com", null, "ADMIN"));

        OrderDto patch = new OrderDto();
        patch.setOrderStatus(OrderStatus.COMPLETED);
        patch.setQuantity(3);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);
        when(orderRepository.findByIdWithRefs(1L)).thenReturn(Optional.of(testOrder));

        orderService.updateOrder(1L, patch);

        assertThat(testOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(testOrder.getQuantity()).isEqualTo(3);
    }

    @Test
    void updateOrderAsUserCannotSetNonCancelledStatus() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("u@test.com", null, "USER"));

        OrderDto patch = new OrderDto();
        patch.setOrderStatus(OrderStatus.SCHEDULED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.updateOrder(1L, patch))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Users can only cancel orders");
    }

    @Test
    void updateOrderAsProviderCannotSetInvalidStatus() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("p@test.com", null, "PROVIDER"));

        OrderDto patch = new OrderDto();
        patch.setOrderStatus(OrderStatus.REQUESTED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.updateOrder(1L, patch))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Providers can only set status to SCHEDULED, COMPLETED, or CANCELLED");
    }

    @Test
    void createOrderProviderNotFoundThrows() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        dto.setProviderId(99L);
        dto.setServiceId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider not found");
    }

    @Test
    void createOrderServiceNotFoundThrows() {
        OrderDto dto = new OrderDto();
        dto.setUserId(1L);
        dto.setProviderId(1L);
        dto.setServiceId(99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(providerRepository.findById(1L)).thenReturn(Optional.of(testProvider));
        when(serviceCatalogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service not found");
    }

    @Test
    void createOrdersBatchUserNotFoundThrows() {
        OrderDto dto = new OrderDto();
        dto.setUserId(99L);
        dto.setProviderId(1L);
        dto.setServiceId(1L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrdersBatch(List.of(dto)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}
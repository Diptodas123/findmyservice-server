package com.FindMyService.service;

import com.FindMyService.model.Order;
import com.FindMyService.model.Provider;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.User;
import com.FindMyService.model.dto.OrderDto;
import com.FindMyService.model.enums.OrderStatus;
import com.FindMyService.repository.OrderRepository;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.UserRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import com.FindMyService.utils.DtoMapper;
import com.FindMyService.utils.ResponseBuilder;
import com.FindMyService.utils.OwnerCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.FindMyService.model.enums.OrderStatus.REQUESTED;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final OwnerCheck ownerCheck;

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAllWithRefs().stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    public Optional<OrderDto> getOrderById(Long orderId) {
        return orderRepository.findByIdWithRefs(orderId).map(DtoMapper::toDto);
    }

    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        User user = userRepository.findById(orderDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + orderDto.getUserId()));

        Provider provider = providerRepository.findById(orderDto.getProviderId())
                .orElseThrow(() -> new IllegalArgumentException("Provider not found with id: " + orderDto.getProviderId()));
        ServiceCatalog service = serviceCatalogRepository.findById(orderDto.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + orderDto.getServiceId()));

        Order order = Order.builder()
                .userId(user)
                .providerId(provider)
                .serviceId(service)
                .orderStatus(REQUESTED)
                .totalCost(orderDto.getTotalCost() != null ? orderDto.getTotalCost() : service.getCost())
                .quantity(orderDto.getQuantity() != null ? orderDto.getQuantity() : 1)
                .requestedDate(orderDto.getRequestedDate())
                .build();

        Order saved = orderRepository.save(order);
        return DtoMapper.toDto(saved);
    }

    @Transactional
    public List<OrderDto> createOrdersBatch(List<OrderDto> orderDtos) {
        // user and provider are the same for all items in a checkout batch
        User user = userRepository.findById(orderDtos.getFirst().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + orderDtos.getFirst().getUserId()));
        Provider provider = providerRepository.findById(orderDtos.getFirst().getProviderId())
                .orElseThrow(() -> new IllegalArgumentException("Provider not found with id: " + orderDtos.getFirst().getProviderId()));

        return orderDtos.stream().map(orderDto -> {
            ServiceCatalog service = serviceCatalogRepository.findById(orderDto.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + orderDto.getServiceId()));
            Order order = Order.builder()
                    .userId(user)
                    .providerId(provider)
                    .serviceId(service)
                    .orderStatus(REQUESTED)
                    .totalCost(orderDto.getTotalCost() != null ? orderDto.getTotalCost() : service.getCost())
                    .quantity(orderDto.getQuantity() != null ? orderDto.getQuantity() : 1)
                    .requestedDate(orderDto.getRequestedDate())
                    .build();
            return DtoMapper.toDto(orderRepository.save(order));
        }).toList();
    }

    @Transactional
    public boolean deleteOrder(Long orderId) {
        return orderRepository.findById(orderId).map(order -> {
            orderRepository.delete(order);
            return true;
        }).orElse(false);
    }

    public ResponseEntity<?> getOrdersByUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseBuilder.build(HttpStatus.NOT_FOUND, "User not found"));
        }

        List<Order> orders = orderRepository.findByUserId(user.get());
        List<OrderDto> orderDtos = orders.stream()
                .map(DtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(orderDtos);
    }

    public ResponseEntity<?> getOrdersByProvider(Long providerId) {
        Optional<Provider> provider = providerRepository.findById(providerId);
        if (provider.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseBuilder.build(HttpStatus.NOT_FOUND, "Provider not found"));
        }

        List<Order> orders = orderRepository.findByProviderId(provider.get());
        List<OrderDto> orderDtos = orders.stream()
                .map(DtoMapper::toDto)
                .toList();
        return ResponseEntity.ok(orderDtos);
    }

    @Transactional
    public OrderDto updateOrder(Long orderId, OrderDto orderDto) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (existingOrder.getOrderStatus() == OrderStatus.COMPLETED ||
            existingOrder.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot update an order with status: " + existingOrder.getOrderStatus());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String userRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.equals("USER") || authority.equals("PROVIDER") || authority.equals("ADMIN"))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Invalid user role"));

        if ("USER".equals(userRole)) {
            ownerCheck.verifyOwner(existingOrder.getUserId().getUserId());
        } else if ("PROVIDER".equals(userRole)) {
            ownerCheck.verifyOwner(existingOrder.getProviderId().getProviderId());
        }

        if ("USER".equals(userRole)) {
            if (orderDto.getScheduledDate() != null) {
                throw new IllegalArgumentException("Users cannot modify scheduledDate");
            }
            if (orderDto.getQuantity() != null) {
                throw new IllegalArgumentException("Users cannot modify quantity");
            }
            if (orderDto.getTotalCost() != null) {
                throw new IllegalArgumentException("Users cannot modify totalCost");
            }

            updateIfNotNull(orderDto.getRequestedDate(), existingOrder::setRequestedDate);

            if (orderDto.getOrderStatus() != null) {
                if (orderDto.getOrderStatus() == OrderStatus.CANCELLED) {
                    existingOrder.setOrderStatus(OrderStatus.CANCELLED);
                } else {
                    throw new IllegalArgumentException("Users can only cancel orders");
                }
            }
        } else if ("PROVIDER".equals(userRole)) {
            if (orderDto.getRequestedDate() != null) {
                throw new IllegalArgumentException("Providers cannot modify requestedDate");
            }
            if (orderDto.getQuantity() != null) {
                throw new IllegalArgumentException("Providers cannot modify quantity");
            }
            if (orderDto.getTotalCost() != null) {
                throw new IllegalArgumentException("Providers cannot modify totalCost");
            }

            updateIfNotNull(orderDto.getScheduledDate(), existingOrder::setScheduledDate);

            if (orderDto.getOrderStatus() != null) {
                if (orderDto.getOrderStatus() == OrderStatus.SCHEDULED ||
                    orderDto.getOrderStatus() == OrderStatus.COMPLETED ||
                    orderDto.getOrderStatus() == OrderStatus.CANCELLED) {
                    existingOrder.setOrderStatus(orderDto.getOrderStatus());
                } else {
                    throw new IllegalArgumentException("Providers can only set status to SCHEDULED, COMPLETED, or CANCELLED");
                }
            }
        } else if ("ADMIN".equals(userRole)) {
            updateIfNotNull(orderDto.getRequestedDate(), existingOrder::setRequestedDate);
            updateIfNotNull(orderDto.getScheduledDate(), existingOrder::setScheduledDate);
            updateIfNotNull(orderDto.getQuantity(), existingOrder::setQuantity);
            updateIfNotNull(orderDto.getOrderStatus(), existingOrder::setOrderStatus);
            updateIfNotNull(orderDto.getTotalCost(), existingOrder::setTotalCost);
        }

        orderRepository.save(existingOrder);
        return orderRepository.findByIdWithRefs(orderId)
                .map(DtoMapper::toDto)
                .orElseThrow();
    }

    private <T> void updateIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}

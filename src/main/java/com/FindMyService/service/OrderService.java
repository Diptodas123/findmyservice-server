package com.FindMyService.service;

import com.FindMyService.model.Order;
import com.FindMyService.model.Provider;
import com.FindMyService.model.User;
import com.FindMyService.model.enums.OrderStatus;
import com.FindMyService.repository.OrderRepository;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import com.FindMyService.repository.UserRepository;
import com.stripe.model.PaymentIntent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static com.FindMyService.model.enums.OrderStatus.PAID;
import static com.FindMyService.model.enums.OrderStatus.REQUESTED;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;
    private final PaymentService paymentService;

    public OrderService(OrderRepository orderRepository,
                        ServiceCatalogRepository serviceCatalogRepository,
                        UserRepository userRepository,
                        ProviderRepository providerRepository,
                        PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.userRepository = userRepository;
        this.providerRepository = providerRepository;
        this.paymentService = paymentService;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional
    public ResponseEntity<?> createOrder(Order order) {
        Optional<User> user = userRepository.findById(order.getUserId().getUserId());
        if (user.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User from payload not found"));
        }

        Optional<Provider> provider = providerRepository.findById(order.getProviderId().getProviderId());
        if (provider.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Provider from payload not found"));
        }

        order.setOrderStatus(REQUESTED);

        Order saved = orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Transactional
    public boolean deleteOrder(Long orderId) {
        return orderRepository.findById(orderId).map(order -> {
            orderRepository.delete(order);
            return true;
        }).orElse(false);
    }

    public List<Order> getOrdersByUser(Long userId) {
        User user = User.builder().userId(userId).build();
        return orderRepository.findByUserId(user);
    }

    public List<Order> getOrdersByProvider(Long providerId) {
        Provider provider = Provider.builder().providerId(providerId).build();
        return orderRepository.findByProviderId(provider);
    }

    @Transactional
    public ResponseEntity<?> initiatePayment(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Order not found"));
        }

        Order existing = order.get();

        if (existing.getOrderStatus() == OrderStatus.COMPLETED ||
                existing.getOrderStatus() == OrderStatus.CANCELLED ||
                existing.getOrderStatus() == OrderStatus.PAID) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Cannot initiate payment for this order"));
        }

        try {
            double priceInRupees = Math.round(existing.getTotalCost().doubleValue() * 100.0) / 100.0;

            Long amountInPaise = (long) (priceInRupees * 100);

            Map<String, String> paymentIntent = paymentService.createPaymentIntent(amountInPaise, orderId);

            existing.setStripePaymentIntentId(paymentIntent.get("paymentIntentId"));
            orderRepository.save(existing);

            return ResponseEntity.ok(Map.of(
                    "clientSecret", paymentIntent.get("clientSecret"),
                    "amountInRupees", priceInRupees,
                    "amountInPaise", amountInPaise,
                    "currency", "INR"
            ));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment initiation failed: " + e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> confirmPayment(Long orderId, String paymentIntentId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Order not found"));
        }

        Order existing = order.get();

        try {
            PaymentIntent paymentIntent = paymentService.confirmPayment(paymentIntentId);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                existing.setOrderStatus(PAID);
                existing.setPaymentDate(Instant.now());
                Order updated = orderRepository.save(existing);
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Payment not successful", "status", paymentIntent.getStatus()));
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment confirmation failed: " + e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Order not found"));
        }

        if (newStatus == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Status cannot be null"));
        }

        Order existing = order.get();
        existing.setOrderStatus(newStatus);
        Order updated = orderRepository.save(existing);
        return ResponseEntity.ok(updated);
    }
}


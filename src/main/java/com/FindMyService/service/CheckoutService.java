package com.FindMyService.service;

import com.FindMyService.model.dto.CheckoutItem;
import com.FindMyService.model.Order;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.User;
import com.FindMyService.repository.ServiceCatalogRepository;
import com.FindMyService.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CheckoutService {

    private final ServiceCatalogRepository serviceCatalogRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    public CheckoutService(ServiceCatalogRepository serviceCatalogRepository,
                           UserRepository userRepository,
                           OrderService orderService) {
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    @Transactional
    public List<ResponseEntity<?>> checkout(Long userId, List<CheckoutItem> items) {
        List<ResponseEntity<?>> results = new ArrayList<>();

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            results.add(ResponseEntity.badRequest().body(Map.of("error", "User not found")));
            return results;
        }
        User user = userOpt.get();

        for (CheckoutItem item : items) {
            Optional<ServiceCatalog> svcOpt = serviceCatalogRepository.findById(item.getServiceId());
            if (svcOpt.isEmpty()) {
                results.add(ResponseEntity.badRequest().body(Map.of("error", "Service not found", "serviceId", item.getServiceId())));
                continue;
            }
            ServiceCatalog svc = svcOpt.get();

            BigDecimal unitPrice = svc.getCost();
            BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            Order order = Order.builder()
                    .serviceId(svc)
                    .userId(user)
                    .providerId(svc.getProviderId())
                    .quantityUnits(item.getQuantity())
                    .requestedDate(item.getRequestedDate())
                    .scheduledDate(item.getScheduledDate())
                    .totalCost(total)
                    .build();

            ResponseEntity<?> res = orderService.createOrder(order);
            results.add(res);
        }

        return results;
    }
}

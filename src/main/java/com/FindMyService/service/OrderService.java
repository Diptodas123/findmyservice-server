package com.FindMyService.service;

import com.FindMyService.model.Order;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    public List<Order> getAllOrders() {
        return Collections.emptyList();
    }

    public Optional<Order> getOrderById(String orderId) {
        return Optional.empty();
    }

    public Order createOrder(Order order) {
        return order;
    }

    public Optional<Order> updateOrder(String orderId, Order order) {
        return Optional.empty();
    }

    public boolean deleteOrder(String orderId) {
        return false;
    }

    public boolean payOrder(String orderId) {
        // placeholder for initiating/recording payment
        return false;
    }
}

package com.FindMyService.repository;

import com.FindMyService.model.Order;
import com.FindMyService.model.Provider;
import com.FindMyService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(User userId);
    List<Order> findByProviderId(Provider providerId);
}

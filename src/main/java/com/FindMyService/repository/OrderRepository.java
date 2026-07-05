package com.FindMyService.repository;

import com.FindMyService.model.Order;
import com.FindMyService.model.Provider;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o join fetch o.userId join fetch o.providerId join fetch o.serviceId")
    List<Order> findAllWithRefs();

    @Query("select o from Order o join fetch o.userId join fetch o.providerId join fetch o.serviceId where o.orderId = :id")
    Optional<Order> findByIdWithRefs(@Param("id") Long id);

    @Query("select o from Order o join fetch o.userId join fetch o.providerId join fetch o.serviceId where o.userId = :user")
    List<Order> findByUserId(@Param("user") User user);

    @Query("select o from Order o join fetch o.userId join fetch o.providerId join fetch o.serviceId where o.providerId = :provider")
    List<Order> findByProviderId(@Param("provider") Provider provider);

    @Modifying
    @Query("delete from Order o where o.userId = :user")
    void deleteByUser(@Param("user") User user);

    @Modifying
    @Query("delete from Order o where o.serviceId = :svc")
    void deleteByService(@Param("svc") ServiceCatalog svc);

    @Modifying
    @Query("delete from Order o where o.providerId.providerId = :providerId")
    void deleteByProviderId(@Param("providerId") Long providerId);
}

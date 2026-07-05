package com.FindMyService.repository;

import com.FindMyService.model.Feedback;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("select f from Feedback f join fetch f.userId join fetch f.serviceId")
    List<Feedback> findAllWithRefs();

    @Query("select f from Feedback f join fetch f.userId join fetch f.serviceId where f.serviceId = :svc")
    List<Feedback> findByServiceId(@Param("svc") ServiceCatalog svc);

    @Query("select f from Feedback f join fetch f.userId join fetch f.serviceId where f.serviceId.providerId.providerId = :providerId")
    List<Feedback> findByProviderId(@Param("providerId") Long providerId);

    @Modifying
    @Query("delete from Feedback f where f.userId = :user")
    void deleteByUser(@Param("user") User user);

    @Modifying
    @Query("delete from Feedback f where f.serviceId = :svc")
    void deleteByService(@Param("svc") ServiceCatalog svc);

    @Modifying
    @Query("delete from Feedback f where f.serviceId.providerId.providerId = :providerId")
    void deleteByProviderId(@Param("providerId") Long providerId);
}

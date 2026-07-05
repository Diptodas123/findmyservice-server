package com.FindMyService.repository;

import com.FindMyService.model.ServiceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, Long> {
    List<ServiceCatalog> findByProviderId_ProviderId(Long providerId);

    @Query("select s from ServiceCatalog s join fetch s.providerId")
    List<ServiceCatalog> findAllWithProvider();

    @Query("select s from ServiceCatalog s join fetch s.providerId where s.serviceId = :id")
    Optional<ServiceCatalog> findByIdWithProvider(@Param("id") Long id);

    @Query("select s from ServiceCatalog s join fetch s.providerId where s.providerId.providerId = :providerId")
    List<ServiceCatalog> findByProviderIdWithProvider(@Param("providerId") Long providerId);

    @Modifying
    @Query("delete from ServiceCatalog s where s.providerId.providerId = :providerId")
    void deleteByProviderId_ProviderId(@Param("providerId") Long providerId);

    @Modifying
    @Query("update ServiceCatalog s set s.avgRating = " +
           "(coalesce(s.avgRating, 0) * s.totalRatings + :rating) / (s.totalRatings + 1), " +
           "s.totalRatings = s.totalRatings + 1 where s.serviceId = :id")
    void incrementRating(@Param("id") Long id, @Param("rating") BigDecimal rating);
}

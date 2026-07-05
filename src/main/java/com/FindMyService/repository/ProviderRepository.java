package com.FindMyService.repository;

import com.FindMyService.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Optional<Provider> findByEmail(String email);
    boolean existsByEmail(String email);

    @Modifying
    @Query("update Provider p set p.avgRating = " +
           "(coalesce(p.avgRating, 0) * p.totalRatings + :rating) / (p.totalRatings + 1), " +
           "p.totalRatings = p.totalRatings + 1 where p.providerId = :id")
    void incrementRating(@Param("id") Long id, @Param("rating") BigDecimal rating);
}

package com.FindMyService.service;

import com.FindMyService.model.Feedback;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.User;
import com.FindMyService.repository.FeedbackRepository;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import com.FindMyService.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ProviderRepository providerRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           UserRepository userRepository,
                           ServiceCatalogRepository serviceCatalogRepository,
                           ProviderRepository providerRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.providerRepository = providerRepository;
    }

    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @Transactional
    public ResponseEntity<?> createFeedback(Feedback feedback) {
        Optional<User> user = userRepository.findById(feedback.getUserId().getUserId());
        if (user.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User from payload not found"));
        }

        Optional<ServiceCatalog> serviceCatalog = serviceCatalogRepository
                .findById(feedback.getServiceId().getServiceId());
        if (serviceCatalog.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Service catalog not found"));
        }

        Feedback saved = feedbackRepository.save(feedback);
        updateRatings(feedback);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Transactional
    public List<Feedback> getAllFeedbacksForService(Long serviceId) {
        ServiceCatalog serviceCatalog = new ServiceCatalog();
        serviceCatalog.setServiceId(serviceId);
        return feedbackRepository.findByServiceId(serviceCatalog);
    }

    void updateRatings(Feedback feedback) {
        ServiceCatalog serviceCatalog = serviceCatalogRepository.findById(feedback.getServiceId().getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));
        com.FindMyService.model.Provider provider = providerRepository.findById(serviceCatalog.getProviderId().getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        int totalServiceReviews = serviceCatalog.getTotalRatings();
        int totalProviderReviews = provider.getTotalRatings();

        int newTotalServiceReviews = totalServiceReviews + 1;
        int newTotalProviderReviews = totalProviderReviews + 1;

        BigDecimal currentProviderRating = provider.getAvgRating() != null ? provider.getAvgRating() : BigDecimal.ZERO;
        BigDecimal updatedProviderRating = currentProviderRating
                .multiply(BigDecimal.valueOf(totalProviderReviews))
                .add(BigDecimal.valueOf(feedback.getRating()))
                .divide(BigDecimal.valueOf(newTotalProviderReviews), 1, java.math.RoundingMode.HALF_UP);

        BigDecimal currentServiceRating = serviceCatalog.getAvgRating() != null ? serviceCatalog.getAvgRating() : BigDecimal.ZERO;
        BigDecimal updatedServiceRating = currentServiceRating
                .multiply(BigDecimal.valueOf(totalServiceReviews))
                .add(BigDecimal.valueOf(feedback.getRating()))
                .divide(BigDecimal.valueOf(newTotalServiceReviews), 1, java.math.RoundingMode.HALF_UP);

        provider.setAvgRating(updatedProviderRating);
        provider.setTotalRatings(newTotalProviderReviews);

        serviceCatalog.setAvgRating(updatedServiceRating);
        serviceCatalog.setTotalRatings(newTotalServiceReviews);

        providerRepository.save(provider);
        serviceCatalogRepository.save(serviceCatalog);
    }

}

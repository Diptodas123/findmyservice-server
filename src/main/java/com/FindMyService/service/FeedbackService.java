package com.FindMyService.service;

import com.FindMyService.model.Feedback;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.User;
import com.FindMyService.model.dto.FeedbackDto;
import com.FindMyService.repository.FeedbackRepository;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import com.FindMyService.repository.UserRepository;
import com.FindMyService.utils.ResponseBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
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
    public ResponseEntity<?> createFeedback(FeedbackDto feedbackDto) {
        if (feedbackDto.getRating().compareTo(BigDecimal.ZERO) < 0 ||
            feedbackDto.getRating().compareTo(new BigDecimal("5.0")) > 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Rating must be between 0 and 5"));
        }

        User user = userRepository.findById(feedbackDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ServiceCatalog serviceCatalog = serviceCatalogRepository.findById(feedbackDto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service catalog not found"));

        Feedback feedback = Feedback.builder()
                .userId(user)
                .serviceId(serviceCatalog)
                .comment(feedbackDto.getComment())
                .rating(feedbackDto.getRating())
                .build();

        Feedback saved = feedbackRepository.save(feedback);

        try {
            updateRatings(saved);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseBuilder.serverError(e.getMessage()));
        }

        FeedbackDto responseDto = FeedbackDto.builder()
                .feedbackId(saved.getFeedbackId())
                .serviceId(saved.getServiceId().getServiceId())
                .userId(saved.getUserId().getUserId())
                .comment(saved.getComment())
                .rating(saved.getRating())
                .createdAt(saved.getCreatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Transactional
    public ResponseEntity<?> getAllFeedbacksForService(Long serviceId) {
        Optional<ServiceCatalog> serviceCatalog = serviceCatalogRepository.findById(serviceId);
        if (serviceCatalog.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseBuilder.build(HttpStatus.NOT_FOUND, "Service not found"));
        }

        List<Feedback> feedbacks = feedbackRepository.findByServiceId(serviceCatalog.get());

        List<FeedbackDto> feedbackDtos = feedbacks.stream()
                .map(feedback -> FeedbackDto.builder()
                        .feedbackId(feedback.getFeedbackId())
                        .serviceId(feedback.getServiceId().getServiceId())
                        .userId(feedback.getUserId().getUserId())
                        .comment(feedback.getComment())
                        .rating(feedback.getRating())
                        .createdAt(feedback.getCreatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(feedbackDtos);
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
                .add(feedback.getRating())
                .divide(BigDecimal.valueOf(newTotalProviderReviews), 2, java.math.RoundingMode.DOWN);

        BigDecimal currentServiceRating = serviceCatalog.getAvgRating() != null ? serviceCatalog.getAvgRating() : BigDecimal.ZERO;
        BigDecimal updatedServiceRating = currentServiceRating
                .multiply(BigDecimal.valueOf(totalServiceReviews))
                .add(feedback.getRating())
                .divide(BigDecimal.valueOf(newTotalServiceReviews), 2, java.math.RoundingMode.DOWN);

        provider.setAvgRating(updatedProviderRating);
        provider.setTotalRatings(newTotalProviderReviews);

        serviceCatalog.setAvgRating(updatedServiceRating);
        serviceCatalog.setTotalRatings(newTotalServiceReviews);

        providerRepository.save(provider);
        serviceCatalogRepository.save(serviceCatalog);
    }
}

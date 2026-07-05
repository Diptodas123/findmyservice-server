package com.FindMyService.service;

import com.FindMyService.model.Feedback;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.User;
import com.FindMyService.model.dto.FeedbackDto;
import com.FindMyService.repository.FeedbackRepository;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import com.FindMyService.repository.UserRepository;
import com.FindMyService.utils.DtoMapper;
import com.FindMyService.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ProviderRepository providerRepository;

    public List<FeedbackDto> getAllFeedbacks() {
        return feedbackRepository.findAllWithRefs().stream()
            .map(DtoMapper::toDto)
            .toList();
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
                    .body(ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toDto(saved));
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
                .map(DtoMapper::toDto)
                .toList();

        return ResponseEntity.ok(feedbackDtos);
    }

    @Transactional
    public ResponseEntity<?> getAllFeedbacksForProvider(Long providerId) {
        List<FeedbackDto> feedbackDtos = feedbackRepository.findByProviderId(providerId).stream()
            .map(DtoMapper::toDto)
            .toList();
        return ResponseEntity.ok(feedbackDtos);
    }

    @Transactional
    void updateRatings(Feedback feedback) {
        Long serviceId = feedback.getServiceId().getServiceId();
        ServiceCatalog serviceCatalog = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        serviceCatalogRepository.incrementRating(serviceId, feedback.getRating());
        providerRepository.incrementRating(serviceCatalog.getProviderId().getProviderId(), feedback.getRating());
    }
}

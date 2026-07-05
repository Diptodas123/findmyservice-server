package com.FindMyService.service;

import com.FindMyService.model.Feedback;
import com.FindMyService.model.Provider;
import com.FindMyService.model.ServiceCatalog;
import com.FindMyService.model.User;
import com.FindMyService.model.dto.FeedbackDto;
import com.FindMyService.repository.FeedbackRepository;
import com.FindMyService.repository.ProviderRepository;
import com.FindMyService.repository.ServiceCatalogRepository;
import com.FindMyService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock private FeedbackRepository feedbackRepository;
    @Mock private UserRepository userRepository;
    @Mock private ServiceCatalogRepository serviceCatalogRepository;
    @Mock private ProviderRepository providerRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private User testUser;
    private ServiceCatalog testService;
    private FeedbackDto testDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);

        Provider testProvider = new Provider();
        testProvider.setProviderId(1L);

        testService = new ServiceCatalog();
        testService.setServiceId(1L);
        testService.setProviderId(testProvider);

        testDto = FeedbackDto.builder()
                .userId(1L)
                .serviceId(1L)
                .rating(new BigDecimal("4.5"))
                .comment("Great!")
                .build();
    }

    @Test
    void createFeedbackReturnsCreated() {
        Feedback saved = Feedback.builder()
                .feedbackId(1L)
                .userId(testUser)
                .serviceId(testService)
                .rating(new BigDecimal("4.5"))
                .comment("Great!")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(serviceCatalogRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(feedbackRepository.save(any())).thenReturn(saved);

        ResponseEntity<?> response = feedbackService.createFeedback(testDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(feedbackRepository).save(any());
    }

    @Test
    void createFeedbackRatingTooHighReturnsBadRequest() {
        testDto.setRating(new BigDecimal("5.1"));
        ResponseEntity<?> response = feedbackService.createFeedback(testDto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void createFeedbackNegativeRatingReturnsBadRequest() {
        testDto.setRating(new BigDecimal("-0.1"));
        ResponseEntity<?> response = feedbackService.createFeedback(testDto);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void createFeedbackUserNotFoundThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> feedbackService.createFeedback(testDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createFeedbackServiceNotFoundThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(serviceCatalogRepository.findById(1L)).thenReturn(Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> feedbackService.createFeedback(testDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Service catalog not found");
    }

    @Test
    void getAllFeedbacksForServiceNotFoundReturnsNotFound() {
        when(serviceCatalogRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseEntity<?> response = feedbackService.getAllFeedbacksForService(99L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllFeedbacksForServiceReturnsOk() {
        when(serviceCatalogRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(feedbackRepository.findByServiceId(testService)).thenReturn(List.of());
        ResponseEntity<?> response = feedbackService.getAllFeedbacksForService(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllFeedbacksForProviderUnknownIdReturnsEmptyOk() {
        when(feedbackRepository.findByProviderId(99L)).thenReturn(List.of());
        ResponseEntity<?> response = feedbackService.getAllFeedbacksForProvider(99L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllFeedbacksForProviderReturnsOk() {
        when(feedbackRepository.findByProviderId(1L)).thenReturn(List.of());
        ResponseEntity<?> response = feedbackService.getAllFeedbacksForProvider(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllFeedbacksReturnsMappedDtos() {
        Feedback f = Feedback.builder()
                .feedbackId(1L)
                .userId(testUser)
                .serviceId(testService)
                .rating(new BigDecimal("4.0"))
                .comment("Nice")
                .build();
        when(feedbackRepository.findAllWithRefs()).thenReturn(List.of(f));

        List<FeedbackDto> result = feedbackService.getAllFeedbacks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFeedbackId()).isEqualTo(1L);
    }
}
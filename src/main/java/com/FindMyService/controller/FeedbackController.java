package com.FindMyService.controller;

import com.FindMyService.model.dto.FeedbackDto;
import com.FindMyService.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/feedbacks")
@RestController
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<FeedbackDto>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<?> getAllFeedbacksForService(@PathVariable Long serviceId) {
        return feedbackService.getAllFeedbacksForService(serviceId);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    public ResponseEntity<?> createFeedback(@RequestBody FeedbackDto feedbackDto) {
        return feedbackService.createFeedback(feedbackDto);
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getAllFeedbacksForProvider(@PathVariable Long providerId) {
        return feedbackService.getAllFeedbacksForProvider(providerId);
    }
}

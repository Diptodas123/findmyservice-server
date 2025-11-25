package com.FindMyService.controller;

import com.FindMyService.model.Feedback;
import com.FindMyService.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/api/v1/feedbacks")
@RestController
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<List<Feedback>> getAllFeedbacksForService(@PathVariable Long orderId) {
        return ResponseEntity.ok(feedbackService.getAllFeedbacksForService(orderId));
    }

    @PostMapping
    public ResponseEntity<?> createFeedback(@RequestBody Feedback feedback) {
        return feedbackService.createFeedback(feedback);
    }
}

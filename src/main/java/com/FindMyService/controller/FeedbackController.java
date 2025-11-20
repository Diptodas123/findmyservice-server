package com.FindMyService.controller;

import com.FindMyService.model.Feedback;
import com.FindMyService.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<Feedback> getFeedback(@PathVariable String feedbackId) {
        return feedbackService.getFeedbackById(feedbackId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@RequestBody Feedback feedback) {
        Feedback feedbackToCreate = feedbackService.createFeedback(feedback);
        if(feedbackToCreate == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackToCreate);
    }
}

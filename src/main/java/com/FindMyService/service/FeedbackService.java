package com.FindMyService.service;

import com.FindMyService.model.Feedback;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    public List<Feedback> getAllFeedbacks() {
        return Collections.emptyList();
    }

    public Optional<Feedback> getFeedbackById(String feedbackId) {
        return Optional.empty();
    }

    public Feedback createFeedback(Feedback feedback) {
        return feedback;
    }
}

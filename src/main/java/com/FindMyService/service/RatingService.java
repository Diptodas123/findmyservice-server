package com.FindMyService.service;

import com.FindMyService.model.Rating;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class RatingService {

    public List<Rating> getAllRatings() {
        return Collections.emptyList();
    }

    public Optional<Rating> getRatingById(String ratingId) {
        return Optional.empty();
    }

    public Rating postRating(Rating rating) {
        return rating;
    }
}

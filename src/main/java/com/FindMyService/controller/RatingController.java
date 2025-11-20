package com.FindMyService.controller;

import com.FindMyService.model.Rating;
import com.FindMyService.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/ratings")
@RestController
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping
    public ResponseEntity<List<Rating>> getAllRatings() {
        return ResponseEntity.ok(ratingService.getAllRatings());
    }

    @GetMapping("/{ratingId}")
    public ResponseEntity<Rating> getRating(@PathVariable String ratingId) {
        return ratingService.getRatingById(ratingId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Rating> postRating(@RequestBody Rating rating) {
        Rating newRating = ratingService.postRating(rating);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRating);
    }
}

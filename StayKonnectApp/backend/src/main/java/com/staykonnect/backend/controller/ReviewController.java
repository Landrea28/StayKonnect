package com.staykonnect.backend.controller;

import com.staykonnect.backend.entity.Review;
import com.staykonnect.backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/property/{propertyId}")
    public List<Review> getPropertyReviews(@PathVariable Long propertyId) {
        return reviewService.getReviewsByProperty(propertyId);
    }

    @GetMapping("/user/{userId}")
    public List<Review> getUserReviews(@PathVariable Long userId) {
        return reviewService.getReviewsByUser(userId);
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.createReview(review));
    }
}

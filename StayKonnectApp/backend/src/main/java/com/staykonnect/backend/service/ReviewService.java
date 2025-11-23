package com.staykonnect.backend.service;

import com.staykonnect.backend.entity.Property;
import com.staykonnect.backend.entity.Review;
import com.staykonnect.backend.entity.User;
import com.staykonnect.backend.repository.PropertyRepository;
import com.staykonnect.backend.repository.ReviewRepository;
import com.staykonnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Review> getReviewsByProperty(Long propertyId) {
        // Validate property exists
        if (!propertyRepository.existsById(propertyId)) {
            throw new RuntimeException("Property not found");
        }
        return reviewRepository.findByTargetPropertyId(propertyId);
    }

    public List<Review> getReviewsByUser(Long userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        return reviewRepository.findByTargetUserId(userId);
    }

    @Transactional
    public Review createReview(Review review) {
        // In a real app, we should validate that the reservation exists and is completed
        // and that the author is part of the reservation.
        return reviewRepository.save(review);
    }
}

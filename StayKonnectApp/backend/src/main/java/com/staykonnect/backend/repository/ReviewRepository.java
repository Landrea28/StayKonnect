package com.staykonnect.backend.repository;

import com.staykonnect.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTargetPropertyId(Long propertyId);
    List<Review> findByTargetUserId(Long userId);
}

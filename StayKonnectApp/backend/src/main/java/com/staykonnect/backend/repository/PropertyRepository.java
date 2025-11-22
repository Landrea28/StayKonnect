package com.staykonnect.backend.repository;

import com.staykonnect.backend.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByHostId(Long hostId);
    
    @Query("SELECT p FROM Property p WHERE " +
           "(:city IS NULL OR p.city LIKE %:city%) AND " +
           "(:country IS NULL OR p.country LIKE %:country%) AND " +
           "(:minPrice IS NULL OR p.pricePerNight >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.pricePerNight <= :maxPrice) AND " +
           "(:guests IS NULL OR p.maxGuests >= :guests) AND " +
           "p.isActive = true AND p.legalStatus = 'VERIFIED'")
    List<Property> findProperties(String city, String country, BigDecimal minPrice, BigDecimal maxPrice, Integer guests);
}

package com.staykonnect.backend.service;

import com.staykonnect.backend.entity.Property;
import com.staykonnect.backend.entity.User;
import com.staykonnect.backend.entity.enums.LegalStatus;
import com.staykonnect.backend.exception.ResourceNotFoundException;
import com.staykonnect.backend.repository.PropertyRepository;
import com.staykonnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
    }

    public List<Property> getPropertiesByHost(Long hostId) {
        return propertyRepository.findByHostId(hostId);
    }

    public List<Property> searchProperties(String city, String country, BigDecimal minPrice, BigDecimal maxPrice, Integer guests) {
        return propertyRepository.findProperties(city, country, minPrice, maxPrice, guests);
    }

    @Transactional
    public Property createProperty(Long hostId, Property property) {
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Host not found with id: " + hostId));
        
        property.setHost(host);
        property.setLegalStatus(LegalStatus.PENDING); // Default status
        return propertyRepository.save(property);
    }

    @Transactional
    public Property updateProperty(Long id, Property propertyDetails) {
        Property property = getPropertyById(id);
        
        property.setTitle(propertyDetails.getTitle());
        property.setDescription(propertyDetails.getDescription());
        property.setPricePerNight(propertyDetails.getPricePerNight());
        property.setMaxGuests(propertyDetails.getMaxGuests());
        property.setAddress(propertyDetails.getAddress());
        property.setCity(propertyDetails.getCity());
        property.setCountry(propertyDetails.getCountry());
        // Update amenities logic would go here
        
        return propertyRepository.save(property);
    }

    @Transactional
    public void deleteProperty(Long id) {
        Property property = getPropertyById(id);
        propertyRepository.delete(property);
    }

    @Transactional
    public void verifyProperty(Long id) {
        Property property = getPropertyById(id);
        property.setLegalStatus(LegalStatus.VERIFIED);
        propertyRepository.save(property);
    }
}

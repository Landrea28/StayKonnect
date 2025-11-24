package com.staykonnect.backend.service;

import com.staykonnect.backend.entity.Property;
import com.staykonnect.backend.entity.Reservation;
import com.staykonnect.backend.entity.User;
import com.staykonnect.backend.entity.enums.ReservationStatus;
import com.staykonnect.backend.exception.BadRequestException;
import com.staykonnect.backend.exception.ResourceNotFoundException;
import com.staykonnect.backend.repository.PropertyRepository;
import com.staykonnect.backend.repository.ReservationRepository;
import com.staykonnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
    }

    public List<Reservation> getReservationsByGuest(Long guestId) {
        return reservationRepository.findByGuestId(guestId);
    }

    public List<Reservation> getReservationsByProperty(Long propertyId) {
        return reservationRepository.findByPropertyId(propertyId);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation createReservation(Long guestId, Long propertyId, Reservation reservationRequest) {
        User guest = userRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (reservationRequest.getStartDate().isAfter(reservationRequest.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        // Check availability (Simplified logic - in real app check overlaps)
        // ...

        long days = ChronoUnit.DAYS.between(reservationRequest.getStartDate(), reservationRequest.getEndDate());
        BigDecimal totalPrice = property.getPricePerNight().multiply(BigDecimal.valueOf(days));

        reservationRequest.setGuest(guest);
        reservationRequest.setProperty(property);
        reservationRequest.setTotalPrice(totalPrice);
        reservationRequest.setStatus(ReservationStatus.PENDING);

        Reservation savedReservation = reservationRepository.save(reservationRequest);

        // Notify Host
        notificationService.createNotification(
            property.getHost().getId(), 
            "RESERVATION_REQUEST", 
            "New reservation request for " + property.getTitle()
        );

        return savedReservation;
    }

    @Transactional
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        
        // Notify Guest
        notificationService.createNotification(
            reservation.getGuest().getId(),
            "RESERVATION_CONFIRMED",
            "Your reservation for " + reservation.getProperty().getTitle() + " has been confirmed."
        );

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservation.setStatus(ReservationStatus.CANCELLED);
        
        // Notify Host
        notificationService.createNotification(
            reservation.getProperty().getHost().getId(),
            "RESERVATION_CANCELLED",
            "Reservation #" + reservationId + " has been cancelled."
        );

        return reservationRepository.save(reservation);
    }
}

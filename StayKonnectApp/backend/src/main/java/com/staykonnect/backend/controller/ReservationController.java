package com.staykonnect.backend.controller;

import com.staykonnect.backend.entity.Reservation;
import com.staykonnect.backend.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/guest/{guestId}")
    public List<Reservation> getReservationsByGuest(@PathVariable Long guestId) {
        return reservationService.getReservationsByGuest(guestId);
    }

    @GetMapping("/property/{propertyId}")
    public List<Reservation> getReservationsByProperty(@PathVariable Long propertyId) {
        return reservationService.getReservationsByProperty(propertyId);
    }

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @RequestParam Long guestId,
            @RequestParam Long propertyId,
            @RequestBody Reservation reservation) {
        return ResponseEntity.ok(reservationService.createReservation(guestId, propertyId, reservation));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Reservation> confirmReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmReservation(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }
}

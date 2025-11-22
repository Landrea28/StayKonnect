package com.staykonnect.backend.service;

import com.staykonnect.backend.entity.Payment;
import com.staykonnect.backend.entity.Reservation;
import com.staykonnect.backend.entity.enums.PaymentStatus;
import com.staykonnect.backend.entity.enums.ReservationStatus;
import com.staykonnect.backend.exception.BadRequestException;
import com.staykonnect.backend.exception.ResourceNotFoundException;
import com.staykonnect.backend.repository.PaymentRepository;
import com.staykonnect.backend.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Payment processPayment(Long reservationId, String transactionId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Cannot pay for a cancelled reservation");
        }

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(reservation.getTotalPrice());
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.HELD); // Funds held initially
        payment.setPaymentDate(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Update reservation status
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        // Notify Host
        notificationService.createNotification(
            reservation.getProperty().getHost().getId(),
            "PAYMENT_RECEIVED",
            "Payment received for reservation #" + reservationId + ". Funds are held."
        );

        return savedPayment;
    }

    @Transactional
    public void releaseFunds(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        // Logic to check if 48h passed after check-in could be here or in a scheduled task
        
        payment.setStatus(PaymentStatus.RELEASED);
        paymentRepository.save(payment);
        
        // Notify Host
        notificationService.createNotification(
            payment.getReservation().getProperty().getHost().getId(),
            "FUNDS_RELEASED",
            "Funds for reservation #" + payment.getReservation().getId() + " have been released."
        );
    }
}

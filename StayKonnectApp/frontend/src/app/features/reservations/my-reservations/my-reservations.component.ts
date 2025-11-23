import { Component, OnInit } from '@angular/core';
import { ReservationService } from '../../../core/services/reservation.service';
import { AuthService } from '../../../core/services/auth.service';
import { Reservation, ReservationStatus } from '../../../core/models/reservation.model';

@Component({
  selector: 'app-my-reservations',
  templateUrl: './my-reservations.component.html',
  styleUrls: ['./my-reservations.component.css']
})
export class MyReservationsComponent implements OnInit {
  reservations: Reservation[] = [];
  loading = true;
  ReservationStatus = ReservationStatus; // Make enum available to template

  constructor(
    private reservationService: ReservationService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    const currentUser = this.authService.currentUserValue;
    if (currentUser) {
      this.reservationService.getReservationsByGuest(currentUser.id).subscribe({
        next: (data) => {
          this.reservations = data;
          this.loading = false;
        },
        error: (e) => {
          console.error(e);
          this.loading = false;
        }
      });
    }
  }

  cancelReservation(id: number | undefined) {
    if (!id) return;
    if (confirm('Are you sure you want to cancel this reservation?')) {
      this.reservationService.cancelReservation(id).subscribe(() => {
        // Update the status locally or reload
        const res = this.reservations.find(r => r.id === id);
        if (res) {
          res.status = ReservationStatus.CANCELLED;
        }
      });
    }
  }
}

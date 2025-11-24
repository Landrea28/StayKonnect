import { Component, OnInit } from '@angular/core';
import { ReservationService } from '../../../core/services/reservation.service';
import { Reservation } from '../../../core/models/reservation.model';

@Component({
  selector: 'app-admin-reservations',
  template: `
    <h2>Manage Reservations</h2>
    <table class="table table-striped">
      <thead>
        <tr>
          <th>ID</th>
          <th>Property</th>
          <th>Guest</th>
          <th>Dates</th>
          <th>Total</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let reservation of reservations">
          <td>{{ reservation.id }}</td>
          <td>{{ reservation.property?.title }}</td>
          <td>{{ reservation.guest?.email }}</td>
          <td>{{ reservation.startDate | date }} - {{ reservation.endDate | date }}</td>
          <td>{{ reservation.totalPrice | currency }}</td>
          <td>{{ reservation.status }}</td>
          <td>
            <button *ngIf="reservation.status === 'PENDING'" class="btn btn-sm btn-success" (click)="confirm(reservation.id)">Confirm</button>
            <button *ngIf="reservation.status !== 'CANCELLED'" class="btn btn-sm btn-danger" (click)="cancel(reservation.id)">Cancel</button>
          </td>
        </tr>
      </tbody>
    </table>
  `
})
export class AdminReservationsComponent implements OnInit {
  reservations: Reservation[] = [];

  constructor(private reservationService: ReservationService) {}

  ngOnInit() {
    this.loadReservations();
  }

  loadReservations() {
    this.reservationService.getAllReservations().subscribe(reservations => {
      this.reservations = reservations;
    });
  }

  confirm(id: number) {
      this.reservationService.confirmReservation(id).subscribe(() => this.loadReservations());
  }

  cancel(id: number) {
      this.reservationService.cancelReservation(id).subscribe(() => this.loadReservations());
  }
}

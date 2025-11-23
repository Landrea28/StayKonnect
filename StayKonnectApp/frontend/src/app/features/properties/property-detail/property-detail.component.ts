import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PropertyService } from '../../../core/services/property.service';
import { ReservationService } from '../../../core/services/reservation.service';
import { PaymentService } from '../../../core/services/payment.service';
import { AuthService } from '../../../core/services/auth.service';
import { Property } from '../../../core/models/property.model';
import { Reservation } from '../../../core/models/reservation.model';
import { switchMap } from 'rxjs/operators';
import { ReviewListComponent } from '../../reviews/review-list/review-list.component';

@Component({
  selector: 'app-property-detail',
  templateUrl: './property-detail.component.html',
  styleUrls: ['./property-detail.component.css']
})
export class PropertyDetailComponent implements OnInit {
  @ViewChild(ReviewListComponent) reviewList!: ReviewListComponent;
  property: Property | undefined;
  loading = true;
  bookingForm: FormGroup;
  totalPrice: number = 0;
  bookingLoading = false;
  bookingError = '';
  bookingSuccess = false;
  currentUser: any = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private propertyService: PropertyService,
    private reservationService: ReservationService,
    private paymentService: PaymentService,
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.bookingForm = this.fb.group({
      startDate: ['', Validators.required],
      endDate: ['', Validators.required]
    });
    
    this.authService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.propertyService.getPropertyById(id).subscribe({
        next: (data) => {
          this.property = data;
          this.loading = false;
        },
        error: (e) => {
          console.error(e);
          this.loading = false;
        }
      });
    }

    this.bookingForm.valueChanges.subscribe(val => {
      this.calculateTotal();
    });
  }

  calculateTotal() {
    if (this.bookingForm.valid && this.property) {
      const start = new Date(this.bookingForm.value.startDate);
      const end = new Date(this.bookingForm.value.endDate);
      
      if (start < end) {
        const diffTime = Math.abs(end.getTime() - start.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        this.totalPrice = diffDays * this.property.pricePerNight;
      } else {
        this.totalPrice = 0;
      }
    } else {
      this.totalPrice = 0;
    }
  }

  onBook() {
    if (this.bookingForm.invalid || !this.property || !this.currentUser) return;

    this.bookingLoading = true;
    this.bookingError = '';

    const reservation: Reservation = {
      startDate: this.bookingForm.value.startDate,
      endDate: this.bookingForm.value.endDate
    };

    this.reservationService.createReservation(this.currentUser.id, this.property.id, reservation)
      .pipe(
        switchMap((createdReservation) => {
          // Mock payment transaction ID
          const transactionId = 'TXN-' + Math.random().toString(36).substr(2, 9).toUpperCase();
          return this.paymentService.processPayment(createdReservation.id!, transactionId);
        })
      )
      .subscribe({
        next: () => {
          this.bookingSuccess = true;
          this.bookingLoading = false;
          this.bookingForm.reset();
        },
        error: (err) => {
          this.bookingError = err.error?.message || 'Failed to process reservation or payment';
          this.bookingLoading = false;
        }
      });
  }

  onReviewSubmitted(): void {
    if (this.reviewList) {
      this.reviewList.loadReviews();
    }
  }
}

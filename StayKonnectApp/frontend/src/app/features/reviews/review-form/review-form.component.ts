import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-review-form',
  templateUrl: './review-form.component.html',
  styleUrls: ['./review-form.component.css']
})
export class ReviewFormComponent {
  @Input() propertyId!: number;
  @Output() reviewSubmitted = new EventEmitter<void>();
  
  reviewForm: FormGroup;
  loading = false;
  error = '';
  success = '';
  hoverRating = 0;

  constructor(
    private fb: FormBuilder,
    private reviewService: ReviewService,
    private authService: AuthService
  ) {
    this.reviewForm = this.fb.group({
      rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  setRating(rating: number): void {
    this.reviewForm.patchValue({ rating });
  }

  onSubmit(): void {
    if (this.reviewForm.invalid) return;

    this.loading = true;
    this.error = '';
    this.success = '';

    const currentUser = this.authService.currentUserValue;
    if (!currentUser) {
      this.error = 'You must be logged in to review.';
      this.loading = false;
      return;
    }

    const reviewData = {
      ...this.reviewForm.value,
      targetProperty: { id: this.propertyId },
      author: { id: currentUser.id }
    };

    this.reviewService.createReview(reviewData).subscribe({
      next: () => {
        this.success = 'Review submitted successfully!';
        this.loading = false;
        this.reviewForm.reset({ rating: 5 });
        this.reviewSubmitted.emit();
      },
      error: (err) => {
        this.error = 'Failed to submit review. You may not be eligible to review this property.';
        this.loading = false;
      }
    });
  }
}

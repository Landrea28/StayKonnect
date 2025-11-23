import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Review } from '../../../core/models/review.model';
import { ReviewService } from '../../../core/services/review.service';

@Component({
  selector: 'app-review-list',
  templateUrl: './review-list.component.html',
  styleUrls: ['./review-list.component.css']
})
export class ReviewListComponent implements OnInit {
  @Input() propertyId!: number;
  reviews: Review[] = [];
  loading = true;

  constructor(private reviewService: ReviewService) {}

  ngOnInit(): void {
    if (this.propertyId) {
      this.loadReviews();
    }
  }

  loadReviews(): void {
    this.reviewService.getReviewsByProperty(this.propertyId).subscribe({
      next: (data) => {
        this.reviews = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading reviews', err);
        this.loading = false;
      }
    });
  }

  getStars(rating: number): number[] {
    return Array(rating).fill(0);
  }
  
  getEmptyStars(rating: number): number[] {
    return Array(5 - rating).fill(0);
  }
}

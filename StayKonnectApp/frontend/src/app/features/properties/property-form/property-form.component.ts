import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PropertyService } from '../../../core/services/property.service';
import { AuthService } from '../../../core/services/auth.service';
import { LegalStatus } from '../../../core/models/property.model';

@Component({
  selector: 'app-property-form',
  templateUrl: './property-form.component.html',
  styleUrls: ['./property-form.component.css']
})
export class PropertyFormComponent {
  propertyForm: FormGroup;
  loading = false;
  submitted = false;
  error = '';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private propertyService: PropertyService,
    private authService: AuthService
  ) {
    this.propertyForm = this.formBuilder.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      address: ['', Validators.required],
      city: ['', Validators.required],
      country: ['', Validators.required],
      pricePerNight: ['', [Validators.required, Validators.min(1)]],
      maxGuests: ['', [Validators.required, Validators.min(1)]]
    });
  }

  get f() { return this.propertyForm.controls; }

  onSubmit() {
    this.submitted = true;

    if (this.propertyForm.invalid) {
      return;
    }

    const currentUser = this.authService.currentUserValue;
    if (!currentUser) {
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;
    
    const propertyData = {
      ...this.propertyForm.value,
      legalStatus: LegalStatus.PENDING,
      isActive: true
    };

    this.propertyService.createProperty(currentUser.id, propertyData)
      .subscribe({
        next: () => {
          this.router.navigate(['/properties']);
        },
        error: error => {
          this.error = error.error ? error.error.message : 'Error creating property';
          this.loading = false;
        }
      });
  }
}

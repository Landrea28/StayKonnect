import { Component, OnInit } from '@angular/core';
import { PropertyService } from '../../../core/services/property.service';
import { AuthService } from '../../../core/services/auth.service';
import { Property } from '../../../core/models/property.model';

@Component({
  selector: 'app-my-properties',
  templateUrl: './my-properties.component.html',
  styleUrls: ['./my-properties.component.css']
})
export class MyPropertiesComponent implements OnInit {
  properties: Property[] = [];
  loading = true;

  constructor(
    private propertyService: PropertyService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    const currentUser = this.authService.currentUserValue;
    if (currentUser) {
      this.propertyService.getPropertiesByHost(currentUser.id).subscribe({
        next: (data) => {
          this.properties = data;
          this.loading = false;
        },
        error: (e) => {
          console.error(e);
          this.loading = false;
        }
      });
    }
  }

  deleteProperty(id: number) {
    if (confirm('Are you sure you want to delete this property?')) {
      this.propertyService.deleteProperty(id).subscribe(() => {
        this.properties = this.properties.filter(p => p.id !== id);
      });
    }
  }
}

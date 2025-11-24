import { Component, OnInit } from '@angular/core';
import { PropertyService } from '../../../core/services/property.service';
import { Property } from '../../../core/models/property.model';

@Component({
  selector: 'app-admin-properties',
  template: `
    <h2>Manage Properties</h2>
    <table class="table table-striped">
      <thead>
        <tr>
          <th>ID</th>
          <th>Title</th>
          <th>Location</th>
          <th>Price</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let property of properties">
          <td>{{ property.id }}</td>
          <td>{{ property.title }}</td>
          <td>{{ property.city }}, {{ property.country }}</td>
          <td>{{ property.pricePerNight | currency }}</td>
          <td>{{ property.legalStatus }}</td>
          <td>
            <button class="btn btn-sm btn-danger" (click)="deleteProperty(property.id)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  `
})
export class AdminPropertiesComponent implements OnInit {
  properties: Property[] = [];

  constructor(private propertyService: PropertyService) {}

  ngOnInit() {
    this.loadProperties();
  }

  loadProperties() {
    this.propertyService.getAllProperties().subscribe(properties => {
      this.properties = properties;
    });
  }

  deleteProperty(id: number) {
    if(confirm('Are you sure?')) {
        this.propertyService.deleteProperty(id).subscribe(() => {
            this.loadProperties();
        });
    }
  }
}

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PropertyService } from '../../../core/services/property.service';
import { Property } from '../../../core/models/property.model';

@Component({
  selector: 'app-property-list',
  templateUrl: './property-list.component.html',
  styleUrls: ['./property-list.component.css']
})
export class PropertyListComponent implements OnInit {
  properties: Property[] = [];
  loading = true;

  constructor(
    private propertyService: PropertyService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const city = params['city'];
      const guests = params['guests'];

      if (city || guests) {
        this.propertyService.searchProperties(city, undefined, undefined, undefined, guests).subscribe({
          next: (data) => {
            this.properties = data;
            this.loading = false;
          },
          error: (e) => {
            console.error(e);
            this.loading = false;
          }
        });
      } else {
        this.propertyService.getAllProperties().subscribe({
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
    });
  }
}

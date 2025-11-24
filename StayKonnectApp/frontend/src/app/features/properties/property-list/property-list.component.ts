import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
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

  // Filter models
  filterCity: string = '';
  filterCountry: string = '';
  filterMinPrice: number | null = null;
  filterMaxPrice: number | null = null;
  filterGuests: number | null = null;

  constructor(
    private propertyService: PropertyService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      console.log('DEBUG: PropertyListComponent queryParams:', params);
      
      // Update local state from params
      this.filterCity = params['city'] || '';
      this.filterCountry = params['country'] || '';
      this.filterMinPrice = params['minPrice'] ? +params['minPrice'] : null;
      this.filterMaxPrice = params['maxPrice'] ? +params['maxPrice'] : null;
      
      const guestsParam = params['guests'];
      let guests: number | undefined = undefined;
      
      if (guestsParam) {
        const parsed = +guestsParam;
        if (!isNaN(parsed)) {
          guests = parsed;
          this.filterGuests = guests;
        }
      } else {
        this.filterGuests = null;
      }
      
      const city = params['city'];
      const country = params['country'];
      const minPrice = params['minPrice'];
      const maxPrice = params['maxPrice'];

      console.log('DEBUG: Parsed params - city:', city, 'guests:', guests);

      if (city || country || minPrice || maxPrice || guests) {
        console.log('DEBUG: Initiating search with filters');
        this.propertyService.searchProperties(city, country, minPrice, maxPrice, guests).subscribe({
          next: (data) => {
            console.log('DEBUG: Search results received:', data);
            this.properties = data;
            this.loading = false;
          },
          error: (e) => {
            console.error('DEBUG: Search error:', e);
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

  applyFilters() {
    const queryParams: any = {};
    if (this.filterCity) queryParams.city = this.filterCity;
    if (this.filterCountry) queryParams.country = this.filterCountry;
    if (this.filterMinPrice) queryParams.minPrice = this.filterMinPrice;
    if (this.filterMaxPrice) queryParams.maxPrice = this.filterMaxPrice;
    if (this.filterGuests) queryParams.guests = this.filterGuests;

    this.router.navigate(['/properties'], { queryParams });
  }

  clearFilters() {
    this.filterCity = '';
    this.filterCountry = '';
    this.filterMinPrice = null;
    this.filterMaxPrice = null;
    this.filterGuests = null;
    this.router.navigate(['/properties']);
  }
}

import { Component, OnInit } from '@angular/core';
import { PropertyService } from '../../core/services/property.service';
import { Property } from '../../core/models/property.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  featuredProperties: Property[] = [];
  searchCity: string = '';
  searchGuests: number | null = null;

  constructor(
    private propertyService: PropertyService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.propertyService.getAllProperties().subscribe(properties => {
      // Just take the first 3 as featured for now
      this.featuredProperties = properties.slice(0, 3);
    });
  }

  onSearch() {
    this.router.navigate(['/properties'], {
      queryParams: {
        city: this.searchCity,
        guests: this.searchGuests
      }
    });
  }
}

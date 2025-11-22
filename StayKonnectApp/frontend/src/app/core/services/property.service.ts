import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Property } from '../models/property.model';

@Injectable({
  providedIn: 'root'
})
export class PropertyService {
  private apiUrl = 'http://localhost:8080/api/properties';

  constructor(private http: HttpClient) { }

  getAllProperties(): Observable<Property[]> {
    return this.http.get<Property[]>(this.apiUrl);
  }

  getPropertyById(id: number): Observable<Property> {
    return this.http.get<Property>(`${this.apiUrl}/${id}`);
  }

  searchProperties(city?: string, country?: string, minPrice?: number, maxPrice?: number, guests?: number): Observable<Property[]> {
    let params = new HttpParams();
    if (city) params = params.set('city', city);
    if (country) params = params.set('country', country);
    if (minPrice) params = params.set('minPrice', minPrice);
    if (maxPrice) params = params.set('maxPrice', maxPrice);
    if (guests) params = params.set('guests', guests);

    return this.http.get<Property[]>(`${this.apiUrl}/search`, { params });
  }

  createProperty(hostId: number, property: Property): Observable<Property> {
      return this.http.post<Property>(`${this.apiUrl}/host/${hostId}`, property);
  }
}

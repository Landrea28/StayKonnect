import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reservation } from '../models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = 'http://localhost:8080/api/reservations';

  constructor(private http: HttpClient) { }

  getAllReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(this.apiUrl);
  }

  getReservationById(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.apiUrl}/${id}`);
  }

  getReservationsByGuest(guestId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.apiUrl}/guest/${guestId}`);
  }

  getReservationsByProperty(propertyId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.apiUrl}/property/${propertyId}`);
  }

  createReservation(guestId: number, propertyId: number, reservation: Reservation): Observable<Reservation> {
    const params = new HttpParams()
      .set('guestId', guestId.toString())
      .set('propertyId', propertyId.toString());
    
    return this.http.post<Reservation>(this.apiUrl, reservation, { params });
  }

  confirmReservation(id: number): Observable<Reservation> {
    return this.http.put<Reservation>(`${this.apiUrl}/${id}/confirm`, {});
  }

  cancelReservation(id: number): Observable<Reservation> {
    return this.http.put<Reservation>(`${this.apiUrl}/${id}/cancel`, {});
  }
}

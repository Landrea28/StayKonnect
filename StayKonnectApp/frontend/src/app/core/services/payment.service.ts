import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Payment {
  id: number;
  reservationId: number;
  amount: number;
  status: 'PENDING' | 'COMPLETED' | 'REFUNDED';
  transactionId: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = 'http://localhost:8080/api/payments';

  constructor(private http: HttpClient) { }

  processPayment(reservationId: number, transactionId: string): Observable<Payment> {
    const params = new HttpParams()
      .set('reservationId', reservationId.toString())
      .set('transactionId', transactionId);
    
    return this.http.post<Payment>(`${this.apiUrl}/process`, {}, { params });
  }

  releaseFunds(paymentId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${paymentId}/release`, {});
  }
}

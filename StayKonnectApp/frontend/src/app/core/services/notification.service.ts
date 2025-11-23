import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, timer } from 'rxjs';
import { switchMap, tap, filter } from 'rxjs/operators';
import { Notification } from '../models/notification.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8080/api/notifications';
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();
  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient, private authService: AuthService) {
    // Poll for notifications every 30 seconds if user is logged in
    timer(0, 30000).pipe(
      filter(() => !!this.authService.currentUserValue),
      switchMap(() => this.getUserNotifications())
    ).subscribe();
  }

  getUserNotifications(): Observable<Notification[]> {
    const user = this.authService.currentUserValue;
    if (!user) return new BehaviorSubject([]).asObservable();

    return this.http.get<Notification[]>(`${this.apiUrl}/user/${user.id}`).pipe(
      tap(notifications => {
        this.notificationsSubject.next(notifications);
        this.updateUnreadCount(notifications);
      })
    );
  }

  markAsRead(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/read`, {}).pipe(
      tap(() => {
        const currentNotifications = this.notificationsSubject.value;
        const updatedNotifications = currentNotifications.map(n => 
          n.id === id ? { ...n, isRead: true } : n
        );
        this.notificationsSubject.next(updatedNotifications);
        this.updateUnreadCount(updatedNotifications);
      })
    );
  }

  private updateUnreadCount(notifications: Notification[]): void {
    const count = notifications.filter(n => !n.isRead).length;
    this.unreadCountSubject.next(count);
  }
}

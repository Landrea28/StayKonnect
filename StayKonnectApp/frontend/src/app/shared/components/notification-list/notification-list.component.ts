import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';
import { Notification } from '../../../core/models/notification.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html',
  styleUrls: ['./notification-list.component.css']
})
export class NotificationListComponent implements OnInit {
  notifications$: Observable<Notification[]>;
  unreadCount$: Observable<number>;
  isOpen = false;

  constructor(private notificationService: NotificationService) {
    this.notifications$ = this.notificationService.notifications$;
    this.unreadCount$ = this.notificationService.unreadCount$;
  }

  ngOnInit(): void {}

  toggleDropdown(): void {
    this.isOpen = !this.isOpen;
  }

  markAsRead(notification: Notification): void {
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id).subscribe();
    }
  }

  getIcon(type: string): string {
    switch (type) {
      case 'RESERVATION_REQUEST': return 'bi-calendar-plus';
      case 'RESERVATION_CONFIRMED': return 'bi-check-circle';
      case 'NEW_MESSAGE': return 'bi-chat-dots';
      case 'PAYMENT_RECEIVED': return 'bi-cash-coin';
      default: return 'bi-bell';
    }
  }
}

import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../../../core/services/notification.service';
import { Notification } from '../../../core/models/notification.model';

@Component({
  selector: 'app-admin-notifications',
  template: `
    <h2>System Notifications</h2>
    <table class="table table-striped">
      <thead>
        <tr>
          <th>ID</th>
          <th>User</th>
          <th>Type</th>
          <th>Message</th>
          <th>Date</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let notification of notifications">
          <td>{{ notification.id }}</td>
          <td>{{ notification.user?.email }}</td>
          <td>{{ notification.type }}</td>
          <td>{{ notification.message }}</td>
          <td>{{ notification.createdAt | date:'medium' }}</td>
        </tr>
      </tbody>
    </table>
  `
})
export class AdminNotificationsComponent implements OnInit {
  notifications: Notification[] = [];

  constructor(private notificationService: NotificationService) {}

  ngOnInit() {
    this.notificationService.getAllNotifications().subscribe(notifications => {
      this.notifications = notifications;
    });
  }
}

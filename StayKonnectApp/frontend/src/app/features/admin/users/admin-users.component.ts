import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-admin-users',
  template: `
    <h2>Manage Users</h2>
    <table class="table table-striped">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Email</th>
          <th>Role</th>
          <th>Verified</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let user of users">
          <td>{{ user.id }}</td>
          <td>{{ user.firstName }} {{ user.lastName }}</td>
          <td>{{ user.email }}</td>
          <td>{{ user.role }}</td>
          <td>{{ user.isVerified ? 'Yes' : 'No' }}</td>
          <td>
            <!-- Add actions like Edit/Delete here -->
            <button class="btn btn-sm btn-primary">Edit</button>
          </td>
        </tr>
      </tbody>
    </table>
  `
})
export class AdminUsersComponent implements OnInit {
  users: User[] = [];

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.userService.getAllUsers().subscribe(users => {
      this.users = users;
    });
  }
}

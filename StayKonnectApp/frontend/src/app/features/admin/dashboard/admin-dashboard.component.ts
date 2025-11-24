import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-dashboard',
  template: `
    <h2>Dashboard</h2>
    <p>Welcome to the StayKonnect Admin Panel.</p>
    <div class="row">
        <div class="col-md-3">
            <div class="card text-white bg-primary mb-3">
                <div class="card-header">Users</div>
                <div class="card-body">
                    <h5 class="card-title">Manage Users</h5>
                    <p class="card-text">View and manage all users.</p>
                    <a routerLink="/admin/users" class="btn btn-light">Go to Users</a>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-white bg-success mb-3">
                <div class="card-header">Properties</div>
                <div class="card-body">
                    <h5 class="card-title">Manage Properties</h5>
                    <p class="card-text">View and manage properties.</p>
                    <a routerLink="/admin/properties" class="btn btn-light">Go to Properties</a>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-white bg-warning mb-3">
                <div class="card-header">Reservations</div>
                <div class="card-body">
                    <h5 class="card-title">Manage Reservations</h5>
                    <p class="card-text">View all reservations.</p>
                    <a routerLink="/admin/reservations" class="btn btn-light">Go to Reservations</a>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-white bg-info mb-3">
                <div class="card-header">Notifications</div>
                <div class="card-body">
                    <h5 class="card-title">System Notifications</h5>
                    <p class="card-text">View system notifications.</p>
                    <a routerLink="/admin/notifications" class="btn btn-light">Go to Notifications</a>
                </div>
            </div>
        </div>
    </div>
  `
})
export class AdminDashboardComponent {}

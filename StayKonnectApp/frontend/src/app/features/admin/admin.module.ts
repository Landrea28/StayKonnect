import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { AdminLayoutComponent } from './layout/admin-layout.component';
import { AdminDashboardComponent } from './dashboard/admin-dashboard.component';
import { AdminUsersComponent } from './users/admin-users.component';
import { AdminPropertiesComponent } from './properties/admin-properties.component';
import { AdminReservationsComponent } from './reservations/admin-reservations.component';
import { AdminNotificationsComponent } from './notifications/admin-notifications.component';

const routes: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'properties', component: AdminPropertiesComponent },
      { path: 'reservations', component: AdminReservationsComponent },
      { path: 'notifications', component: AdminNotificationsComponent }
    ]
  }
];

@NgModule({
  declarations: [
    AdminLayoutComponent,
    AdminDashboardComponent,
    AdminUsersComponent,
    AdminPropertiesComponent,
    AdminReservationsComponent,
    AdminNotificationsComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class AdminModule { }

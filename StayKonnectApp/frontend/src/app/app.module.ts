import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { PropertyListComponent } from './features/properties/property-list/property-list.component';
import { PropertyDetailComponent } from './features/properties/property-detail/property-detail.component';
import { PropertyFormComponent } from './features/properties/property-form/property-form.component';
import { MyPropertiesComponent } from './features/properties/my-properties/my-properties.component';
import { MyReservationsComponent } from './features/reservations/my-reservations/my-reservations.component';
import { ChatComponent } from './features/messaging/chat/chat.component';
import { ReviewListComponent } from './features/reviews/review-list/review-list.component';
import { ReviewFormComponent } from './features/reviews/review-form/review-form.component';
import { NotificationListComponent } from './shared/components/notification-list/notification-list.component';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    LoginComponent,
    RegisterComponent,
    PropertyListComponent,
    PropertyDetailComponent,
    PropertyFormComponent,
    MyPropertiesComponent,
    MyReservationsComponent,
    ChatComponent,
    ReviewListComponent,
    ReviewFormComponent,
    NotificationListComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

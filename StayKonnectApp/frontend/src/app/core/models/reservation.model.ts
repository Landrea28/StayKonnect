import { Property } from './property.model';
import { User } from './user.model';

export enum ReservationStatus {
    PENDING = 'PENDING',
    CONFIRMED = 'CONFIRMED',
    CANCELLED = 'CANCELLED',
    COMPLETED = 'COMPLETED'
}

export interface Reservation {
    id?: number;
    property?: Property;
    guest?: User;
    startDate: string; // ISO date string YYYY-MM-DD
    endDate: string;   // ISO date string YYYY-MM-DD
    totalPrice?: number;
    status?: ReservationStatus;
    createdAt?: string;
    updatedAt?: string;
}

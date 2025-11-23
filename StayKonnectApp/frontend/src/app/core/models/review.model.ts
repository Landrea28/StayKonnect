import { User } from './user.model';
import { Property } from './property.model';

export interface Review {
    id?: number;
    reservationId?: number; // Assuming we send ID for creation
    author?: User;
    targetProperty?: Property;
    targetUser?: User;
    rating: number;
    comment: string;
    createdAt?: string;
}

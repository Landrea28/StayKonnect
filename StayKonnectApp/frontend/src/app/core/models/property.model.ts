import { User } from './user.model';

export enum LegalStatus {
    PENDING = 'PENDING',
    VERIFIED = 'VERIFIED',
    REJECTED = 'REJECTED'
}

export interface Property {
    id: number;
    host?: User;
    title: string;
    description: string;
    address: string;
    city: string;
    country: string;
    pricePerNight: number;
    maxGuests: number;
    legalStatus: LegalStatus;
    isActive: boolean;
    images?: PropertyImage[];
    amenities?: Amenity[];
    createdAt?: string;
    updatedAt?: string;
}

export interface PropertyImage {
    id: number;
    imageUrl: string;
}

export interface Amenity {
    id: number;
    name: string;
}

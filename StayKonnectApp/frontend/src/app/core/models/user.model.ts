export enum Role {
    HOST = 'HOST',
    TRAVELER = 'TRAVELER',
    ADMIN = 'ADMIN'
}

export interface User {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    role: Role;
    isVerified: boolean;
    createdAt?: string;
    updatedAt?: string;
}

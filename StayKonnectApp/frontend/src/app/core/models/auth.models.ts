import { Role } from './user.model';

export interface AuthRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    role: Role;
}

export interface AuthResponse {
    token: string;
    id: number;
    email: string;
    role: string;
}

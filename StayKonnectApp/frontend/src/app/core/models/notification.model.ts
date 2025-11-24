import { User } from './user.model';

export interface Notification {
    id: number;
    userId: number;
    user?: User;
    type: string;
    message: string;
    isRead: boolean;
    createdAt: string;
}

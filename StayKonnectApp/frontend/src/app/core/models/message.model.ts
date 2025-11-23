import { User } from './user.model';

export interface Message {
    id: number;
    sender: User;
    receiver: User;
    content: string;
    sentAt?: string; // Assuming backend provides this, usually mapped from LocalDateTime
    isRead?: boolean;
}

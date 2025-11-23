export interface Notification {
    id: number;
    userId: number;
    type: string;
    message: string;
    isRead: boolean;
    createdAt: string;
}

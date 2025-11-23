import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Message } from '../models/message.model';

@Injectable({
  providedIn: 'root'
})
export class MessageService {
  private apiUrl = 'http://localhost:8080/api/messages';

  constructor(private http: HttpClient) { }

  getChatHistory(user1Id: number, user2Id: number): Observable<Message[]> {
    const params = new HttpParams()
      .set('user1Id', user1Id.toString())
      .set('user2Id', user2Id.toString());
    
    return this.http.get<Message[]>(`${this.apiUrl}/history`, { params });
  }

  sendMessage(senderId: number, receiverId: number, content: string): Observable<Message> {
    const params = new HttpParams()
      .set('senderId', senderId.toString())
      .set('receiverId', receiverId.toString());
    
    // The backend expects the body to be just the string content
    return this.http.post<Message>(`${this.apiUrl}/send`, content, { params });
  }
}

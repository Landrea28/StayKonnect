import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MessageService } from '../../../core/services/message.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { Message } from '../../../core/models/message.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, AfterViewChecked {
  @ViewChild('scrollMe') private myScrollContainer!: ElementRef;
  
  messages: Message[] = [];
  currentUser: any;
  otherUser: User | undefined;
  newMessage: string = '';
  loading = true;
  sending = false;

  constructor(
    private route: ActivatedRoute,
    private messageService: MessageService,
    private authService: AuthService,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    const otherUserId = Number(this.route.snapshot.paramMap.get('userId'));

    if (this.currentUser && otherUserId) {
      // Load other user details
      this.userService.getUserById(otherUserId).subscribe(user => {
        this.otherUser = user;
      });

      // Load chat history
      this.loadMessages(otherUserId);
    }
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }

  loadMessages(otherUserId: number) {
    this.messageService.getChatHistory(this.currentUser.id, otherUserId).subscribe({
      next: (data) => {
        this.messages = data;
        this.loading = false;
        this.scrollToBottom();
      },
      error: (e) => {
        console.error(e);
        this.loading = false;
      }
    });
  }

  sendMessage() {
    if (!this.newMessage.trim() || !this.otherUser) return;

    this.sending = true;
    this.messageService.sendMessage(this.currentUser.id, this.otherUser.id, this.newMessage)
      .subscribe({
        next: (msg) => {
          this.messages.push(msg);
          this.newMessage = '';
          this.sending = false;
          this.scrollToBottom();
        },
        error: (e) => {
          console.error(e);
          this.sending = false;
        }
      });
  }
}

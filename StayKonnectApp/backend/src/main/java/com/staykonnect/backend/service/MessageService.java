package com.staykonnect.backend.service;

import com.staykonnect.backend.entity.Message;
import com.staykonnect.backend.entity.User;
import com.staykonnect.backend.exception.ResourceNotFoundException;
import com.staykonnect.backend.repository.MessageRepository;
import com.staykonnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Message> getChatHistory(Long user1Id, Long user2Id) {
        return messageRepository.findChatHistory(user1Id, user2Id);
    }

    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);

        // Notify Receiver
        notificationService.createNotification(
            receiverId,
            "NEW_MESSAGE",
            "You have a new message from " + sender.getFirstName()
        );

        return savedMessage;
    }
}

package com.staykonnect.backend.controller;

import com.staykonnect.backend.entity.Message;
import com.staykonnect.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/history")
    public List<Message> getChatHistory(@RequestParam Long user1Id, @RequestParam Long user2Id) {
        return messageService.getChatHistory(user1Id, user2Id);
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestBody String content) {
        return ResponseEntity.ok(messageService.sendMessage(senderId, receiverId, content));
    }
}

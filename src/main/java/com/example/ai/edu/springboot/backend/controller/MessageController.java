package com.example.ai.edu.springboot.backend.controller;

import org.springframework.web.bind.annotation.*;
import com.example.ai.edu.springboot.backend.model.Message;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @GetMapping
    public Message getMessage() {
        return new Message("Hello from GET!");
    }

    @PostMapping
    public Message postMessage(@RequestBody Message message) {
        return new Message("Received: " + message.getText());
    }
}

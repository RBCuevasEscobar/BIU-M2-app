package com.ecommerce.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import com.ecommerce.service.ChatService;
import com.ecommerce.dto.ChatRequest;
import com.ecommerce.dto.ChatResponse;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {

        System.out.println("Mensaje recibido: " + request.getMessage());

        String respuesta = chatService.procesarPregunta(request.getMessage());

        return ResponseEntity.ok(new ChatResponse(respuesta));
    }
}
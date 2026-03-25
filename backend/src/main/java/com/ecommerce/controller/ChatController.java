package com.ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.dto.ChatRequest;
import com.ecommerce.dto.ChatResponse;
import com.ecommerce.security.UsuarioSecurity;
import com.ecommerce.service.ChatMemoryService;
import com.ecommerce.service.ChatService;

/**
 * ChatController — Endpoints del asistente virtual.
 *
 *  POST   /api/chat             → enviar mensaje y obtener respuesta del AI
 *  DELETE /api/chat/historial   → limpiar el historial en memoria del usuario
 *                                 (llamado por el frontend en cada logout)
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatMemoryService memoryService;

    public ChatController(ChatService chatService, ChatMemoryService memoryService) {
        this.chatService   = chatService;
        this.memoryService = memoryService;
    }

    // ── Helper: resolver userId desde el principal JWT ────────────────────────
    private Long resolverUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioSecurity user) {
            return user.getId();
        }
        return null;
    }

    // ── POST /api/chat ────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        Long userId = resolverUserId();
        String respuesta = chatService.procesarPregunta(userId, request.getMessage());
        return ResponseEntity.ok(new ChatResponse(respuesta));
    }

    // ── DELETE /api/chat/historial ────────────────────────────────────────────
    /**
     * Limpia el historial en memoria del usuario autenticado.
     * Es invocado por auth.js al hacer logout para que la próxima sesión
     * del usuario inicie con un contexto de conversación limpio.
     */
    @DeleteMapping("/historial")
    public ResponseEntity<Void> limpiarHistorial() {
        Long userId = resolverUserId();
        if (userId != null) {
            memoryService.limpiar(userId);
        }
        return ResponseEntity.noContent().build();
    }
}
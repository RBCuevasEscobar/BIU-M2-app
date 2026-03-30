package com.ecommerce.service;

import com.ecommerce.model.ChatMensaje;
import com.ecommerce.repository.ChatMensajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMemoryService {

    private final ChatMensajeRepository chatMensajeRepository;
    private static final int MAX_HISTORY = 10;

    public ChatMemoryService(ChatMensajeRepository chatMensajeRepository) {
        this.chatMensajeRepository = chatMensajeRepository;
    }

    public List<String> obtenerHistorial(Long userId) {
        return chatMensajeRepository.findByUsuarioIdOrderByFechaEnvioAsc(userId)
                .stream()
                .map(ChatMensaje::getContenido)
                .collect(Collectors.toList());
    }

    @Transactional
    public void agregarMensaje(Long userId, String mensaje) {
        chatMensajeRepository.save(new ChatMensaje(userId, mensaje));

        List<ChatMensaje> historial = chatMensajeRepository.findByUsuarioIdOrderByFechaEnvioAsc(userId);
        if (historial.size() > MAX_HISTORY) {
            ChatMensaje oldestMessage = historial.get(0);
            chatMensajeRepository.delete(oldestMessage);
        }
    }

    @Transactional
    public void limpiar(Long userId) {
        chatMensajeRepository.deleteByUsuarioId(userId);
    }
}

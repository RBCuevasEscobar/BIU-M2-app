package com.ecommerce.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatMemoryService {

    // userId → historial
    private final Map<Long, List<String>> conversaciones = new HashMap<>();

    private static final int MAX_HISTORY = 10;

    public List<String> obtenerHistorial(Long userId) {
        return conversaciones.getOrDefault(userId, new ArrayList<>());
    }

    public void agregarMensaje(Long userId, String mensaje) {

        conversaciones.putIfAbsent(userId, new ArrayList<>());

        List<String> historial = conversaciones.get(userId);

        historial.add(mensaje);

        // limitar tamaño
        if (historial.size() > MAX_HISTORY) {
            historial.remove(0);
        }
    }

    public void limpiar(Long userId) {
        conversaciones.remove(userId);
    }
}

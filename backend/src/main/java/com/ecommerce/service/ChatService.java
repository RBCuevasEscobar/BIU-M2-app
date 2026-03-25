package com.ecommerce.service;

import com.ecommerce.dto.ProductoDTO;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

@Service
public class ChatService {

    private final ProductoService productoService;
    private final ChatClient chatClient;
    private final ChatMemoryService memoryService;

    public ChatService(
            ProductoService productoService,
            ChatClient.Builder builder,
            ChatMemoryService memoryService) {

        this.productoService = productoService;
        this.chatClient = builder.build();
        this.memoryService = memoryService;
    }

    public String procesarPregunta(Long userId, String pregunta) {

        List<ProductoDTO> productos = productoService.listarProductosPublicos();

        String contextoProductos = productos.stream()
                .limit(15)
                .map(p -> p.getNombre() + " - $" + p.getPrecio() + " " + p.getDescripcion())
                .reduce("", (a, b) -> a + "\n" + b);

        // 🔥 HISTORIAL
        List<String> historial = memoryService.obtenerHistorial(userId);

        String historialTexto = String.join("\n", historial);

        String prompt = """
                Eres un asistente inteligente exclusivamente de una tienda eCommerce.

                Tu unico objetivo es ayudar al usuario con:
                - información de productos
                - recomendaciones
                - precios
                - procesos de compra

                Si el usuario te hace preguntas sobre cualquier otro tema que no sea relacionado
                con la tienda eCommerce (cocina, politica, religion, etc.), debes responder cortesmente:
                "Lo siento, solo puedo ayudarte con temas relacionados con la tienda eCommerce."

                Las respuestas retornadas a las preguntas validas del usuario, se deben responder de la
                manera mas concreta posible, es decir, proporcionar la respuesta a la pregunta sin agregar
                detalles adicionales, a menos que el usuario lo solicite expresamente.

                Historial de conversación:
                %s

                Productos disponibles:
                %s

                Pregunta del usuario:
                %s
                """.formatted(historialTexto, contextoProductos, pregunta);

        String respuesta = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 🔥 Guardar conversación
        memoryService.agregarMensaje(userId, "Usuario: " + pregunta);
        memoryService.agregarMensaje(userId, "ChatBot: " + respuesta);

        return respuesta;
    }
}
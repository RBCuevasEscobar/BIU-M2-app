package com.ecommerce.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_mensajes")
public class ChatMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, length = 2000)
    private String contenido;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio;

    public ChatMensaje() {
    }

    public ChatMensaje(Long usuarioId, String contenido) {
        this.usuarioId = usuarioId;
        this.contenido = contenido;
        this.fechaEnvio = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
}

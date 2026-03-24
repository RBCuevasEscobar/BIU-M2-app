package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Cola de notificaciones pendientes para el administrador.
 * Se genera cuando el ADMIN no está activo en el sistema.
 * Se entrega (y elimina) la próxima vez que el ADMIN inicia sesión.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "notificaciones_pendientes")
public class NotificacionPendiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Boolean leida = false;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }

    public NotificacionPendiente(String mensaje) {
        this.mensaje = mensaje;
        this.leida = false;
    }
}

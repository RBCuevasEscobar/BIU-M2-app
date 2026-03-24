package com.ecommerce.controller;

import com.ecommerce.model.NotificacionPendiente;
import com.ecommerce.repository.NotificacionPendienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador de notificaciones pendientes para el administrador.
 * 
 * GET  /api/notificaciones/pendientes  — Lista todas las no leídas
 * POST /api/notificaciones/ack         — Marca lista de IDs como leídas (ACK)
 */
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionPendienteRepository notificacionRepo;

    /**
     * Retorna todas las notificaciones pendientes (no leídas).
     * Solo ADMIN puede consumir este endpoint.
     */
    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificacionPendiente>> getPendientes() {
        List<NotificacionPendiente> pendientes = notificacionRepo.findByLeidaFalseOrderByFechaAsc();
        return ResponseEntity.ok(pendientes);
    }

    /**
     * Acepta una lista de IDs y marca esas notificaciones como leídas (ACK).
     * Body: { "ids": [1, 2, 3] }
     */
    @PostMapping("/ack")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> ack(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids != null && !ids.isEmpty()) {
            List<NotificacionPendiente> notificaciones = notificacionRepo.findAllById(ids);
            notificaciones.forEach(n -> n.setLeida(true));
            notificacionRepo.saveAll(notificaciones);
        }
        return ResponseEntity.ok().build();
    }
}

package com.ecommerce.controller;

import com.ecommerce.model.Orden;
import com.ecommerce.service.OrdenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    @PostMapping("/checkout")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Orden> checkout() {
        return ResponseEntity.ok(ordenService.crearOrdenDesdeCarrito());
    }

    @GetMapping
    public ResponseEntity<List<Orden>> listarOrdenes() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.ok(ordenService.listarTodas());
        }
        return ResponseEntity.ok(ordenService.listarOrdenesPropias());
    }

    @GetMapping("/usuario/{usuarioId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or @usuarioSecurity.isOwner(#usuarioId)")
    public ResponseEntity<List<Orden>> listarOrdenesUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(ordenService.listarOrdenesUsuario(usuarioId));
    }
}

package com.ecommerce.controller;

import com.ecommerce.dto.DireccionDTO;
import com.ecommerce.dto.DireccionRequestDTO;
import com.ecommerce.model.Usuario;
import com.ecommerce.service.DireccionService;
import com.ecommerce.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/direcciones")
public class DireccionController {

    @Autowired
    private DireccionService direccionService;

    @Autowired
    private UsuarioService usuarioService;

    private Usuario getAuthenticatedUser(Authentication auth) {
        return usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'SUPPLIER')")
    public ResponseEntity<DireccionDTO> crearMiDireccion(@Valid @RequestBody DireccionRequestDTO request,
            Authentication auth) {
        Usuario accionUsuario = getAuthenticatedUser(auth);
        return ResponseEntity.ok(direccionService.crearDireccion(accionUsuario, request));
    }

    @PostMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DireccionDTO> crearDireccionParaUsuario(@PathVariable Long usuarioId,
            @Valid @RequestBody DireccionRequestDTO request) {
        Usuario targetUser = usuarioService.buscarPorId(usuarioId);
        return ResponseEntity.ok(direccionService.crearDireccion(targetUser, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'SUPPLIER')")
    public ResponseEntity<DireccionDTO> actualizarDireccion(@PathVariable Long id,
            @Valid @RequestBody DireccionRequestDTO request, Authentication auth) {
        Usuario accionUsuario = getAuthenticatedUser(auth);
        return ResponseEntity.ok(direccionService.actualizarDireccion(id, request, accionUsuario));
    }

    @GetMapping("/mis-direcciones")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'SUPPLIER')")
    public ResponseEntity<List<DireccionDTO>> listarMisDirecciones(Authentication auth) {
        Usuario accionUsuario = getAuthenticatedUser(auth);
        return ResponseEntity.ok(direccionService.listarMisDirecciones(accionUsuario));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'SUPPLIER')")
    public ResponseEntity<DireccionDTO> obtenerDireccion(@PathVariable Long id, Authentication auth) {
        Usuario accionUsuario = getAuthenticatedUser(auth);
        return ResponseEntity.ok(direccionService.obtenerDireccion(id, accionUsuario));
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'SUPPLIER')")
    public ResponseEntity<List<DireccionDTO>> listarDireccionesDeUsuario(@PathVariable Long usuarioId,
            Authentication auth) {
        Usuario accionUsuario = getAuthenticatedUser(auth);
        return ResponseEntity.ok(direccionService.listarDireccionesPorUsuarioId(usuarioId, accionUsuario));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DireccionDTO>> listarTodasLasDirecciones(Authentication auth) {
        Usuario accionUsuario = getAuthenticatedUser(auth);
        return ResponseEntity.ok(direccionService.listarTodasLasDirecciones(accionUsuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<Void> eliminarDireccion(@PathVariable Long id, Authentication auth) {
        Usuario accionUsuario = getAuthenticatedUser(auth);
        direccionService.eliminarDireccion(id, accionUsuario);
        return ResponseEntity.noContent().build();
    }
}

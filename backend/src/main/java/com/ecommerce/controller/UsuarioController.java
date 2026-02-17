package com.ecommerce.controller;

import com.ecommerce.model.Usuario;
import com.ecommerce.model.Cliente;
import com.ecommerce.model.Proveedor;
import com.ecommerce.model.Administrador;
import com.ecommerce.service.UsuarioService;
import com.ecommerce.service.AdministradorService;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/cliente")
    public ResponseEntity<Usuario> registrarCliente(@RequestBody Cliente cliente) {
        cliente.setRole(com.ecommerce.model.Role.CUSTOMER);
        return ResponseEntity.ok(usuarioService.registrarUsuario(cliente));
    }

    @PostMapping("/proveedor")
    public ResponseEntity<Usuario> registrarProveedor(@RequestBody Proveedor proveedor) {
        proveedor.setRole(com.ecommerce.model.Role.SUPPLIER);
        return ResponseEntity.ok(usuarioService.registrarUsuario(proveedor));
    }

    @PostMapping("/admin")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> registrarAdmin(@RequestBody Administrador admin) {
        admin.setRole(com.ecommerce.model.Role.ADMIN);
        return ResponseEntity.ok(usuarioService.registrarUsuario(admin));
    }

    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> listarUsuarios(Authentication auth) {
        String email = auth.getName();
        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
        AdministradorService.validarAdminVigente(usuario);
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or @usuarioSecurity.isOwner(#id)")
    public ResponseEntity<Usuario> listarUsuario(@PathVariable Long id) {
        return usuarioService.listarUsuario(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario datosActualizados) {
        try {
            Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, datosActualizados);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

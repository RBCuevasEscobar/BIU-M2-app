package com.ecommerce.controller;

import com.ecommerce.model.Usuario;
import com.ecommerce.model.Cliente;
import com.ecommerce.model.Proveedor;
import com.ecommerce.dto.UsuarioUpdateRequest;
import com.ecommerce.model.Administrador;
import com.ecommerce.service.UsuarioService;
import com.ecommerce.service.AdministradorService;
import com.ecommerce.security.UsuarioSecurity;

import org.springframework.http.HttpStatus;
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

    /**
     * Obtener usuario por ID.
     * - ADMIN: puede ver cualquier usuario.
     * - CUSTOMER / SUPPLIER: solo puede ver su propio perfil.
     *
     * NOTA: El @PreAuthorize anterior usaba @usuarioSecurity.isOwner(#id),
     * que dependía de un bean Spring que ya no existe. Ahora la lógica de
     * propiedad se valida directamente con el principal del SecurityContext.
     */
    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'SUPPLIER')")
    public ResponseEntity<Usuario> listarUsuario(@PathVariable Long id, Authentication auth) {

        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!esAdmin) {
            // Para roles no-ADMIN, validar que el usuario solo pueda ver su propio perfil
            if (auth.getPrincipal() instanceof UsuarioSecurity principal) {
                if (!principal.getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return usuarioService.listarUsuario(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioUpdateRequest dto) {
        try {
            return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto));
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

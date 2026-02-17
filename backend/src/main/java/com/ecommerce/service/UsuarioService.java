package com.ecommerce.service;

import com.ecommerce.model.Usuario;
import com.ecommerce.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @SuppressWarnings("null")
    public Optional<Usuario> listarUsuario(Long id) {
        return usuarioRepository.findById(id);
    }

    @SuppressWarnings("null")
    public Optional<Usuario> obtenerUsuario(Long id) {
        return usuarioRepository.findById(id);
    }

    // @SuppressWarnings("null")
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public com.ecommerce.dto.LoginResponse login(com.ecommerce.dto.LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        System.out.println("Las credenciales ingresadas son: " + request.email() + " " + request.password());

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // Simulate JWT generation for Phase 2
        String token = "mock-jwt-token-" + usuario.getId();
        return new com.ecommerce.dto.LoginResponse(usuario.getId(), usuario.getNombre(), usuario.getRole(), token);
    }

    @SuppressWarnings("null")
    @Transactional
    public Usuario actualizarUsuario(Long id, Usuario datosActualizados) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Update basic fields
        if (datosActualizados.getNombre() != null) {
            usuario.setNombre(datosActualizados.getNombre());
        }

        if (datosActualizados.getEmail() != null && !datosActualizados.getEmail().equals(usuario.getEmail())) {
            // Check if new email already exists
            if (usuarioRepository.existsByEmail(datosActualizados.getEmail())) {
                throw new RuntimeException("El email ya está registrado");
            }
            usuario.setEmail(datosActualizados.getEmail());
        }

        // Update password only if provided
        if (datosActualizados.getPassword() != null && !datosActualizados.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(datosActualizados.getPassword()));
        }

        // Update role if provided
        if (datosActualizados.getRole() != null) {
            usuario.setRole(datosActualizados.getRole());
        }

        // Update birth date if provided
        if (datosActualizados.getFechaNacimiento() != null) {
            usuario.setFechaNacimiento(datosActualizados.getFechaNacimiento());
        }

        return usuarioRepository.save(usuario);
    }

    @SuppressWarnings("null")
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

}

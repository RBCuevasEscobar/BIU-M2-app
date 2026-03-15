package com.ecommerce.service;

import com.ecommerce.dto.UsuarioUpdateRequest;
import com.ecommerce.model.Cliente;
import com.ecommerce.model.Usuario;
import com.ecommerce.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private com.ecommerce.security.JwtProvider jwtProvider;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (usuario instanceof Cliente cliente) {
            validarRfcCurp(cliente);
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

    /**
     * Busca un usuario por ID; lanza excepción si no existe.
     * Usado por DireccionController para operaciones sobre destinatarios
     * específicos.
     */
    @SuppressWarnings("null")
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
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

        // Generate rigorous JSON Web Token using Provider
        String token = jwtProvider.generateToken(usuario.getEmail(), usuario.getId(), usuario.getRole().name());
        
        return new com.ecommerce.dto.LoginResponse(usuario.getId(), usuario.getNombre(), usuario.getRole(), token);
    }

    @SuppressWarnings("null")
    @Transactional
    public Usuario actualizarUsuario(Long id, UsuarioUpdateRequest datosActualizados) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Update basic fields
        if (datosActualizados.nombre() != null) {
            usuario.setNombre(datosActualizados.nombre());
        }

        if (datosActualizados.email() != null && !datosActualizados.email().equals(usuario.getEmail())) {
            // Check if new email already exists
            if (usuarioRepository.existsByEmail(datosActualizados.email())) {
                throw new RuntimeException("El email ya está registrado");
            }
            usuario.setEmail(datosActualizados.email());
        }

        // Update password only if provided
        if (datosActualizados.password() != null && !datosActualizados.password().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(datosActualizados.password()));
        }

        // Update role if provided
        if (datosActualizados.role() != null) {
            usuario.setRole(datosActualizados.role());
        }

        // Update birth date if provided
        if (datosActualizados.fechaNacimiento() != null) {
            usuario.setFechaNacimiento(datosActualizados.fechaNacimiento());
        }

        // Update rfcCurp if applies to Cliente
        if (usuario instanceof com.ecommerce.model.Cliente cliente) {
            if (datosActualizados.rfcCurp() != null && !datosActualizados.rfcCurp().trim().isEmpty()) {
                cliente.setRfcCurp(datosActualizados.rfcCurp().trim());
            }
        }

        // Update empresa if applies to Proveedor
        if (usuario instanceof com.ecommerce.model.Proveedor proveedor) {
            if (datosActualizados.empresa() != null && !datosActualizados.empresa().trim().isEmpty()) {
                proveedor.setEmpresa(datosActualizados.empresa().trim());
            }
        }

        // Update validUntil if applies to Administrador
        if (usuario instanceof com.ecommerce.model.Administrador admin) {
            if (datosActualizados.validUntil() != null) {
                admin.setValidUntil(datosActualizados.validUntil());
            }
        }

        return usuarioRepository.save(usuario);
    }

    @SuppressWarnings("null")
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    private static final Pattern RFC_PATTERN = Pattern.compile("^[A-Za-z]{4}[0-9]{6}[A-Za-z0-9]{3}$");

    private static final Pattern CURP_PATTERN = Pattern.compile("^[A-Za-z]{4}[0-9]{6}[HM][A-Za-z]{5}[0-9]{2}$");

    private void validarRfcCurp(Cliente cliente) {

        String value = cliente.getRfcCurp();

        if (value == null || value.isBlank())
            throw new RuntimeException("RFC/CURP es obligatorio");

        if (!(RFC_PATTERN.matcher(value).matches() ||
                CURP_PATTERN.matcher(value).matches()))
            throw new RuntimeException("RFC/CURP inválido");

        String fechaNacimiento = cliente.getFechaNacimiento()
                .format(DateTimeFormatter.ofPattern("yyMMdd"));

        String fechaDocumento = value.substring(4, 10);

        if (!fechaNacimiento.equals(fechaDocumento))
            throw new RuntimeException(
                    "La fecha en RFC/CURP no coincide con la fecha de nacimiento");
    }
}

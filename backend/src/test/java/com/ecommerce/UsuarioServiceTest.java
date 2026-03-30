package com.ecommerce;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.LoginResponse;
import com.ecommerce.dto.UsuarioUpdateRequest;
import com.ecommerce.exception.ConfiguracionInvalidaException;
import com.ecommerce.exception.UsuarioNoEncontradoException;
import com.ecommerce.model.Cliente;
import com.ecommerce.model.Role;
import com.ecommerce.model.Usuario;
import com.ecommerce.repository.UsuarioRepository;
import com.ecommerce.security.JwtProvider;
import com.ecommerce.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de UsuarioService con Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService — Tests Unitarios")
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder   passwordEncoder;
    @Mock private JwtProvider       jwtProvider;

    @InjectMocks
    private UsuarioService usuarioService;

    private Cliente clienteValido;

    @BeforeEach
    void setUp() {
        // RFC válido: 4 letras + 6 dígitos de fecha (yyMMdd) + 3 alfanuméricos
        // Fecha nacimiento: 1990-06-15 → yyMMdd = 900615
        clienteValido = new Cliente();
        clienteValido.setId(1L);
        clienteValido.setNombre("Juan Pérez");
        clienteValido.setEmail("juan@test.com");
        clienteValido.setPassword("hashed_pass");
        clienteValido.setRole(Role.CUSTOMER);
        clienteValido.setFechaNacimiento(LocalDate.of(1990, 6, 15));
        clienteValido.setRfcCurp("PERJ900615AB1");
    }

    // ── registrarUsuario ──────────────────────────────────────────────────────
    @Test
    @DisplayName("registrarUsuario: email duplicado → UsuarioNoEncontradoException (USR_002)")
    void registrar_emailDuplicado() {
        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(true);
        UsuarioNoEncontradoException ex = assertThrows(UsuarioNoEncontradoException.class,
                () -> usuarioService.registrarUsuario(clienteValido));
        assertEquals("USR_002", ex.getCodigo());
    }

    @Test
    @DisplayName("registrarUsuario: RFC vacío → ConfiguracionInvalidaException (CFG_002)")
    void registrar_rfcVacio() {
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        clienteValido.setRfcCurp("");
        ConfiguracionInvalidaException ex = assertThrows(ConfiguracionInvalidaException.class,
                () -> usuarioService.registrarUsuario(clienteValido));
        assertEquals("CFG_002", ex.getCodigo());
    }

    @Test
    @DisplayName("registrarUsuario: RFC con formato inválido → ConfiguracionInvalidaException")
    void registrar_rfcFormato_invalido() {
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        clienteValido.setRfcCurp("BADFORMAT123");
        assertThrows(ConfiguracionInvalidaException.class,
                () -> usuarioService.registrarUsuario(clienteValido));
    }

    @Test
    @DisplayName("registrarUsuario: RFC válido → guarda usuario con password encriptado")
    void registrar_exitoso() {
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenReturn(clienteValido);

        Usuario guardado = usuarioService.registrarUsuario(clienteValido);
        assertNotNull(guardado);
        verify(passwordEncoder).encode(any());
        verify(usuarioRepository).save(any());
    }

    // ── login ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("login: usuario no existe → UsuarioNoEncontradoException (USR_003)")
    void login_usuarioNoExiste() {
        when(usuarioRepository.findByEmail("nadie@x.com")).thenReturn(Optional.empty());
        LoginRequest req = new LoginRequest("nadie@x.com", "pass123");
        UsuarioNoEncontradoException ex = assertThrows(UsuarioNoEncontradoException.class,
                () -> usuarioService.login(req));
        assertEquals("USR_003", ex.getCodigo());
    }

    @Test
    @DisplayName("login: password incorrecta → UsuarioNoEncontradoException (USR_003)")
    void login_passwordIncorrecta() {
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(clienteValido));
        when(passwordEncoder.matches("wrongpass", "hashed_pass")).thenReturn(false);
        LoginRequest req = new LoginRequest("juan@test.com", "wrongpass");
        UsuarioNoEncontradoException ex = assertThrows(UsuarioNoEncontradoException.class,
                () -> usuarioService.login(req));
        assertEquals("USR_003", ex.getCodigo());
    }

    @Test
    @DisplayName("login: credenciales correctas → retorna LoginResponse con token")
    void login_exitoso() {
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(clienteValido));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtProvider.generateToken(any(), any(), any())).thenReturn("jwt.token.here");
        LoginRequest req = new LoginRequest("juan@test.com", "correctpass");
        LoginResponse resp = usuarioService.login(req);
        assertNotNull(resp);
        assertEquals("jwt.token.here", resp.token());
    }

    // ── buscarPorId ───────────────────────────────────────────────────────────
    @Test
    @DisplayName("buscarPorId: usuario no existe → UsuarioNoEncontradoException (USR_001)")
    void buscarPorId_noExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());
        UsuarioNoEncontradoException ex = assertThrows(UsuarioNoEncontradoException.class,
                () -> usuarioService.buscarPorId(999L));
        assertEquals("USR_001", ex.getCodigo());
        assertEquals(404, ex.getHttpStatus());
    }

    @Test
    @DisplayName("buscarPorId: usuario existe → retorna usuario")
    void buscarPorId_existe() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(clienteValido));
        Usuario u = usuarioService.buscarPorId(1L);
        assertEquals("juan@test.com", u.getEmail());
    }

    // ── actualizarUsuario ─────────────────────────────────────────────────────
    @Test
    @DisplayName("actualizarUsuario: usuario no encontrado → UsuarioNoEncontradoException")
    void actualizar_noExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        UsuarioUpdateRequest req = new UsuarioUpdateRequest(null, null, null, null, null, null, null, null);
        assertThrows(UsuarioNoEncontradoException.class,
                () -> usuarioService.actualizarUsuario(99L, req));
    }

    @Test
    @DisplayName("actualizarUsuario: email duplicado → UsuarioNoEncontradoException (USR_002)")
    void actualizar_emailDuplicado() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(clienteValido));
        when(usuarioRepository.existsByEmail("otro@test.com")).thenReturn(true);
        UsuarioUpdateRequest req = new UsuarioUpdateRequest("Juan", "otro@test.com", null, null, null, null, null, null);
        UsuarioNoEncontradoException ex = assertThrows(UsuarioNoEncontradoException.class,
                () -> usuarioService.actualizarUsuario(1L, req));
        assertEquals("USR_002", ex.getCodigo());
    }
}

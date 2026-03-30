package com.ecommerce;

import com.ecommerce.exception.ProductoNoEncontradoException;
import com.ecommerce.exception.UsuarioNoEncontradoException;
import com.ecommerce.model.*;
import com.ecommerce.repository.CarritoRepository;
import com.ecommerce.repository.ProductoRepository;
import com.ecommerce.repository.UsuarioRepository;
import com.ecommerce.service.CarritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de CarritoService con Mockito.
 * Se mockea el SecurityContextHolder para simular el usuario autenticado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CarritoService — Tests Unitarios")
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CarritoService carritoService;

    private Cliente clienteTest;
    private ProductoFisico laptopTest;
    private Carrito carritoTest;

    @BeforeEach
    void setUp() {
        clienteTest = new Cliente();
        clienteTest.setId(1L);
        clienteTest.setEmail("cliente@test.com");
        clienteTest.setRole(Role.CUSTOMER);

        laptopTest = new ProductoFisico("Laptop Test", 20000.0, 5, 2.0);

        carritoTest = new Carrito();
        carritoTest.setUsuario(clienteTest);
    }

    // ── Helper: mockear SecurityContext con usuario autenticado ───────────────
    private void mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // ── obtenerCarritoActual ──────────────────────────────────────────────────
    @Test
    @DisplayName("obtenerCarritoActual: usuario no encontrado → UsuarioNoEncontradoException")
    void obtenerCarrito_usuarioNoEncontrado() {
        mockSecurityContext("nadie@x.com");
        when(usuarioRepository.findByEmail("nadie@x.com")).thenReturn(Optional.empty());
        assertThrows(UsuarioNoEncontradoException.class,
                () -> carritoService.obtenerCarritoActual());
    }

    @Test
    @DisplayName("obtenerCarritoActual: crea carrito si no existe")
    void obtenerCarrito_creaCarritoNuevo() {
        mockSecurityContext("cliente@test.com");
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(clienteTest));
        when(carritoRepository.findByUsuario(clienteTest)).thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carritoTest);

        Carrito resultado = carritoService.obtenerCarritoActual();
        assertNotNull(resultado);
        verify(carritoRepository).save(any(Carrito.class));
    }

    // ── agregarProducto(Long) [SOBRECARGA 1] ──────────────────────────────────
    @Test
    @DisplayName("agregarProducto(Long): producto no encontrado → ProductoNoEncontradoException")
    void agregarProducto_productoNoEncontrado() {
        mockSecurityContext("cliente@test.com");
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(clienteTest));
        when(carritoRepository.findByUsuario(clienteTest)).thenReturn(Optional.of(carritoTest));
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoException.class,
                () -> carritoService.agregarProducto(999L));
    }

    @Test
    @DisplayName("agregarProducto(Long): producto válido → agrega al carrito y guarda")
    void agregarProducto_exitoso() {
        mockSecurityContext("cliente@test.com");
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(clienteTest));
        when(carritoRepository.findByUsuario(clienteTest)).thenReturn(Optional.of(carritoTest));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(laptopTest));
        when(carritoRepository.save(any())).thenReturn(carritoTest);

        Carrito result = carritoService.agregarProducto(1L);
        assertNotNull(result);
        assertTrue(result.getProductos().contains(laptopTest));
    }

    // ── agregarProducto(Producto) [SOBRECARGA 2] ──────────────────────────────
    @Test
    @DisplayName("agregarProducto(Producto): delega a agregarProducto(Long) correctamente")
    void agregarProducto_sobrecargaObjeto() {
        mockSecurityContext("cliente@test.com");
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(clienteTest));
        when(carritoRepository.findByUsuario(clienteTest)).thenReturn(Optional.of(carritoTest));
        when(productoRepository.findById(any())).thenReturn(Optional.of(laptopTest));
        when(carritoRepository.save(any())).thenReturn(carritoTest);

        // Usar objeto Producto directamente (SOBRECARGA 2)
        Carrito result = carritoService.agregarProducto(laptopTest);
        assertNotNull(result);
    }

    // ── eliminarProducto ──────────────────────────────────────────────────────
    @Test
    @DisplayName("eliminarProducto: producto no encontrado → ProductoNoEncontradoException")
    void eliminarProducto_noEncontrado() {
        mockSecurityContext("cliente@test.com");
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(clienteTest));
        when(carritoRepository.findByUsuario(clienteTest)).thenReturn(Optional.of(carritoTest));
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoException.class,
                () -> carritoService.eliminarProducto(999L));
    }

    // ── vaciarCarrito ─────────────────────────────────────────────────────────
    @Test
    @DisplayName("vaciarCarrito: limpia la lista de productos y guarda")
    void vaciarCarrito_ok() {
        carritoTest.getProductos().add(laptopTest);
        mockSecurityContext("cliente@test.com");
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(clienteTest));
        when(carritoRepository.findByUsuario(clienteTest)).thenReturn(Optional.of(carritoTest));
        when(carritoRepository.save(any())).thenReturn(carritoTest);

        Carrito result = carritoService.vaciarCarrito();
        assertTrue(result.getProductos().isEmpty());
        verify(carritoRepository).save(any());
    }
}

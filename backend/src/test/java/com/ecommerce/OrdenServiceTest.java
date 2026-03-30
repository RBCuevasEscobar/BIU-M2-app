package com.ecommerce;

import com.ecommerce.dto.OrdenDTO;
import com.ecommerce.exception.CarritoVacioException;
import com.ecommerce.exception.OrdenNoValidaException;
import com.ecommerce.exception.UsuarioNoEncontradoException;
import com.ecommerce.inventory.GestorInventario;
import com.ecommerce.inventory.GestorInventarioFactory;
import com.ecommerce.model.*;
import com.ecommerce.payment.ProcesoPago;
import com.ecommerce.payment.ProcesoPagoFactory;
import com.ecommerce.repository.*;
import com.ecommerce.service.OrdenService;
import com.ecommerce.service.ConfiguracionSistemaService;
import com.ecommerce.config.ConfiguracionSistema;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de OrdenService con Mockito.
 *
 * IMPORTANTE: El ciclo de la orden y la máquina de estados son probados
 * a nivel de excepción (verificar que las excepciones correctas se lanzan
 * cuando el estado no permite la operación), SIN modificar la lógica de negocio.
 *
 * Estados del ciclo de la orden:
 * CREATED → PAYMENT_PENDING → PAID → SHIPPED → DELIVERED
 *                           ↘ OUT_OF_STOCK
 *              (cualquiera) → CANCELLED (si no está PAID/SHIPPED/DELIVERED)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrdenService — Tests Unitarios (Ciclo de Orden)")
class OrdenServiceTest {

    @Mock private OrdenRepository              ordenRepository;
    @Mock private UsuarioRepository            usuarioRepository;
    @Mock private CarritoRepository            carritoRepository;
    @Mock private DireccionRepository          direccionRepository;
    @Mock private PaymentTransactionRepository paymentTransactionRepository;
    @Mock private ShipmentRepository           shipmentRepository;
    @Mock private GestorInventarioFactory      inventarioFactory;
    @Mock private ApplicationEventPublisher    eventPublisher;
    @Mock private ProcesoPagoFactory           pagoFactory;
    @Mock private ConfiguracionSistemaService  configService;

    @InjectMocks
    private OrdenService ordenService;

    private Cliente     cliente;
    private Carrito     carrito;
    private Orden       orden;
    private ProductoFisico producto;
    private GestorInventario gestorInventario;
    private ProcesoPago procesoPago;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setEmail("cliente@test.com");
        cliente.setRole(Role.CUSTOMER);
        cliente.setRfcCurp("PERJ900615AB1");

        producto = new ProductoFisico("Laptop", 25000.0, 10, 2.5);
        producto.setProveedor("TechCorp");

        carrito = new Carrito();
        carrito.setUsuario(cliente);
        carrito.getProductos().add(producto);

        orden = new Orden();
        orden.setId(1L);
        orden.setUsuario(cliente);
        orden.setEstado(EstadoOrden.CREATED);

        OrdenDetalle detalle = new OrdenDetalle(1, 25000.0, producto);
        orden.agregarDetalle(detalle);
        orden.setSubtotalProductos(25000.0);
        orden.setIvaTasa(0.16);
        orden.setTotal(29000.0);

        gestorInventario = mock(GestorInventario.class);
        procesoPago = mock(ProcesoPago.class);

        ConfiguracionSistema csMock = new ConfiguracionSistema();
        csMock.setMaxProductosOrden(10);
        csMock.setIva(0.16);
        when(configService.getConfiguracionSistema()).thenReturn(csMock);

        // Mockear SecurityContext para getUsuarioActual()
        mockSecurityContext("cliente@test.com");
    }

    private void mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // crearOrdenDesdeCarrito
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("crearOrden: usuario no encontrado → UsuarioNoEncontradoException")
    void crearOrden_usuarioNoEncontrado() {
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.empty());
        assertThrows(UsuarioNoEncontradoException.class,
                () -> ordenService.crearOrdenDesdeCarrito(null));
    }

    @Test
    @DisplayName("crearOrden: carrito no encontrado → CarritoVacioException")
    void crearOrden_carritoNoEncontrado() {
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByUsuario(cliente)).thenReturn(Optional.empty());
        assertThrows(CarritoVacioException.class,
                () -> ordenService.crearOrdenDesdeCarrito(null));
    }

    @Test
    @DisplayName("crearOrden: carrito vacío → CarritoVacioException")
    void crearOrden_carritoVacio() {
        carrito.getProductos().clear();
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByUsuario(cliente)).thenReturn(Optional.of(carrito));
        assertThrows(CarritoVacioException.class,
                () -> ordenService.crearOrdenDesdeCarrito(null));
    }

    @Test
    @DisplayName("crearOrden: MAX_ITEMS excedido → OrdenNoValidaException (ORD_002)")
    void crearOrden_maxItemsExcedido() {
        ConfiguracionSistema csMock1 = new ConfiguracionSistema();
        csMock1.setMaxProductosOrden(1);
        csMock1.setIva(0.16);
        when(configService.getConfiguracionSistema()).thenReturn(csMock1);

        // Forzar config con maxItems = 1 — carrito ya tiene 1 item (totalItems >= maxItems)
        // Para simular esto, agregamos más productos al carrito de los que permite el config
        for (int i = 0; i < 50; i++) carrito.getProductos().add(producto);

        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByUsuario(cliente)).thenReturn(Optional.of(carrito));
        doNothing().when(eventPublisher).publishEvent(any());

        OrdenNoValidaException ex = assertThrows(OrdenNoValidaException.class,
                () -> ordenService.crearOrdenDesdeCarrito(null));
        assertEquals("ORD_002", ex.getCodigo());
    }

    @Test
    @DisplayName("crearOrden: exitoso → retorna OrdenDTO con estado CREATED")
    void crearOrden_exitoso() {
        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByUsuario(cliente)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any())).thenReturn(carrito);
        when(ordenRepository.save(any())).thenReturn(orden);
        doNothing().when(eventPublisher).publishEvent(any());

        OrdenDTO result = ordenService.crearOrdenDesdeCarrito(null);
        assertNotNull(result);
        assertEquals("CREATED", result.getEstado());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // procesarPago — Máquina de Estados (SIN modificar el ciclo de la orden)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("procesarPago: orden no encontrada → OrdenNoValidaException (ORD_001)")
    void procesarPago_ordenNoEncontrada() {
        when(ordenRepository.findById(99L)).thenReturn(Optional.empty());
        OrdenNoValidaException ex = assertThrows(OrdenNoValidaException.class,
                () -> ordenService.procesarPago(99L, "Tarjeta", null));
        assertEquals("ORD_001", ex.getCodigo());
    }

    @Test
    @DisplayName("procesarPago: estado PAID → OrdenNoValidaException (ORD_003) — transición inválida")
    void procesarPago_yaFuePagada() {
        orden.setEstado(EstadoOrden.PAID);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        OrdenNoValidaException ex = assertThrows(OrdenNoValidaException.class,
                () -> ordenService.procesarPago(1L, "Tarjeta", null));
        assertEquals("ORD_003", ex.getCodigo());
    }

    @Test
    @DisplayName("procesarPago: estado CANCELLED → OrdenNoValidaException (ORD_003)")
    void procesarPago_cancelada() {
        orden.setEstado(EstadoOrden.CANCELLED);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        OrdenNoValidaException ex = assertThrows(OrdenNoValidaException.class,
                () -> ordenService.procesarPago(1L, "Tarjeta", null));
        assertEquals("ORD_003", ex.getCodigo());
    }

    @Test
    @DisplayName("procesarPago: stock insuficiente → estado OUT_OF_STOCK")
    void procesarPago_sinStock() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(inventarioFactory.obtenerGestor(any())).thenReturn(gestorInventario);
        when(gestorInventario.verificarStock(any(), anyInt())).thenReturn(false);

        Orden ordenSinStock = new Orden();
        ordenSinStock.setId(1L);
        ordenSinStock.setEstado(EstadoOrden.OUT_OF_STOCK);
        ordenSinStock.setUsuario(cliente);
        ordenSinStock.setSubtotalProductos(25000.0);
        ordenSinStock.setIvaTasa(0.16);
        ordenSinStock.setTotal(29000.0);
        when(ordenRepository.save(any())).thenReturn(ordenSinStock);

        OrdenDTO result = ordenService.procesarPago(1L, "Tarjeta", null);
        assertEquals("OUT_OF_STOCK", result.getEstado());
    }

    @Test
    @DisplayName("procesarPago: pago exitoso → estado PAID")
    void procesarPago_exitoso() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(inventarioFactory.obtenerGestor(any())).thenReturn(gestorInventario);
        when(gestorInventario.verificarStock(any(), anyInt())).thenReturn(true);
        when(pagoFactory.obtenerMetodo("Tarjeta")).thenReturn(procesoPago);
        doNothing().when(procesoPago).iniciarPago(any());
        when(procesoPago.verificarPago(any())).thenReturn(true);
        when(procesoPago.confirmarPago(any(), anyDouble())).thenReturn(null);
        doNothing().when(eventPublisher).publishEvent(any());

        Orden ordenPaid = new Orden();
        ordenPaid.setId(1L);
        ordenPaid.setEstado(EstadoOrden.PAID);
        ordenPaid.setUsuario(cliente);
        ordenPaid.setSubtotalProductos(25000.0);
        ordenPaid.setIvaTasa(0.16);
        ordenPaid.setTotal(29000.0);
        when(ordenRepository.save(any())).thenReturn(ordenPaid);

        OrdenDTO result = ordenService.procesarPago(1L, "Tarjeta", null);
        assertEquals("PAID", result.getEstado());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // cancelarOrden
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("cancelarOrden: estado PAID → no se puede cancelar (ORD_003)")
    void cancelarOrden_yaFuePagada() {
        orden.setEstado(EstadoOrden.PAID);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Mockear auth para validarPropietarioOAdmin
        mockSecurityContext("cliente@test.com");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        when(auth.getAuthorities()).thenReturn(java.util.Collections.emptyList());

        OrdenNoValidaException ex = assertThrows(OrdenNoValidaException.class,
                () -> ordenService.cancelarOrden(1L));
        assertEquals("ORD_003", ex.getCodigo());
    }

    @Test
    @DisplayName("cancelarOrden: estado CREATED → cancelación exitosa")
    void cancelarOrden_exitoso() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));

        // Auth mock for validarPropietarioOAdmin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        when(auth.getAuthorities()).thenReturn(java.util.Collections.emptyList());
        when(auth.getName()).thenReturn("cliente@test.com");

        Orden ordenCancelada = new Orden();
        ordenCancelada.setId(1L);
        ordenCancelada.setEstado(EstadoOrden.CANCELLED);
        ordenCancelada.setUsuario(cliente);
        ordenCancelada.setSubtotalProductos(25000.0);
        ordenCancelada.setIvaTasa(0.16);
        ordenCancelada.setTotal(29000.0);
        when(ordenRepository.save(any())).thenReturn(ordenCancelada);
        doNothing().when(eventPublisher).publishEvent(any());

        OrdenDTO result = ordenService.cancelarOrden(1L);
        assertEquals("CANCELLED", result.getEstado());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // despacharOrden / entregarOrden
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("despacharOrden: estado CREATED → no se puede despachar (ORD_003)")
    void despacharOrden_estadoInvalido() {
        // Estado CREATED no permite despacho (solo PAID)
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        OrdenNoValidaException ex = assertThrows(OrdenNoValidaException.class,
                () -> ordenService.despacharOrden(1L, "FedEx", "TRK123"));
        assertEquals("ORD_003", ex.getCodigo());
    }

    @Test
    @DisplayName("entregarOrden: estado PAID → no se puede entregar (debe ser SHIPPED)")
    void entregarOrden_estadoInvalido() {
        orden.setEstado(EstadoOrden.PAID);
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        OrdenNoValidaException ex = assertThrows(OrdenNoValidaException.class,
                () -> ordenService.entregarOrden(1L));
        assertEquals("ORD_003", ex.getCodigo());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // listarOrdenesUsuario
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("listarOrdenesUsuario: usuario no encontrado → UsuarioNoEncontradoException")
    void listarOrdenes_usuarioNoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UsuarioNoEncontradoException.class,
                () -> ordenService.listarOrdenesUsuario(99L));
    }
}

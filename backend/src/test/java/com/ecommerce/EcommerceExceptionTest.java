package com.ecommerce;

import com.ecommerce.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la jerarquía de excepciones de dominio.
 * Valida: código de error, HTTP status, mensaje, herencia y factory methods.
 */
@DisplayName("EcommerceException — Jerarquía y Códigos")
class EcommerceExceptionTest {

    // ── EcommerceException base ───────────────────────────────────────────────
    @Test
    @DisplayName("Toda excepción de dominio extiende EcommerceException")
    void todasExtiendenBase() {
        assertInstanceOf(EcommerceException.class, new ProductoNoEncontradoException(1L));
        assertInstanceOf(EcommerceException.class, new UsuarioNoEncontradoException(1L));
        assertInstanceOf(EcommerceException.class, new CarritoVacioException());
        assertInstanceOf(EcommerceException.class, new InventarioInsuficienteException("Laptop", 2, 5));
        assertInstanceOf(EcommerceException.class, OrdenNoValidaException.noEncontrada(1L));
        assertInstanceOf(EcommerceException.class, new PagoFallidoException("PayPal"));
        assertInstanceOf(EcommerceException.class, new AccesoDenegadoException());
        assertInstanceOf(EcommerceException.class, new ConfiguracionInvalidaException("IVA inválido"));
    }

    // ── ProductoNoEncontradoException ─────────────────────────────────────────
    @Test
    @DisplayName("ProductoNoEncontradoException: código PROD_001, HTTP 404")
    void productoNoEncontrado_codigo_status() {
        ProductoNoEncontradoException ex = new ProductoNoEncontradoException(42L);
        assertEquals("PROD_001", ex.getCodigo());
        assertEquals(404, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("42"));
    }

    // ── UsuarioNoEncontradoException ──────────────────────────────────────────
    @Test
    @DisplayName("UsuarioNoEncontradoException: código USR_001, HTTP 404 (por ID)")
    void usuarioNoEncontrado_porId() {
        UsuarioNoEncontradoException ex = new UsuarioNoEncontradoException(99L);
        assertEquals("USR_001", ex.getCodigo());
        assertEquals(404, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    @DisplayName("UsuarioNoEncontradoException.emailDuplicado: código USR_002, HTTP 409")
    void usuarioNoEncontrado_emailDuplicado() {
        UsuarioNoEncontradoException ex = UsuarioNoEncontradoException.emailDuplicado("test@test.com");
        assertEquals("USR_002", ex.getCodigo());
        assertEquals(409, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("test@test.com"));
    }

    @Test
    @DisplayName("UsuarioNoEncontradoException.credencialesInvalidas: código USR_003, HTTP 401")
    void usuarioNoEncontrado_credencialesInvalidas() {
        UsuarioNoEncontradoException ex = UsuarioNoEncontradoException.credencialesInvalidas();
        assertEquals("USR_003", ex.getCodigo());
        assertEquals(401, ex.getHttpStatus());
    }

    // ── CarritoVacioException ─────────────────────────────────────────────────
    @Test
    @DisplayName("CarritoVacioException: código CART_001, HTTP 400")
    void carritoVacio() {
        CarritoVacioException ex = new CarritoVacioException();
        assertEquals("CART_001", ex.getCodigo());
        assertEquals(400, ex.getHttpStatus());
        assertNotNull(ex.getMessage());
    }

    // ── InventarioInsuficienteException ───────────────────────────────────────
    @Test
    @DisplayName("InventarioInsuficienteException: código INV_001, HTTP 409, mensaje con datos")
    void inventarioInsuficiente() {
        InventarioInsuficienteException ex = new InventarioInsuficienteException("Laptop", 2, 10);
        assertEquals("INV_001", ex.getCodigo());
        assertEquals(409, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("Laptop"));
        assertTrue(ex.getMessage().contains("2"));
        assertTrue(ex.getMessage().contains("10"));
    }

    // ── OrdenNoValidaException ────────────────────────────────────────────────
    @Test
    @DisplayName("OrdenNoValidaException.noEncontrada: código ORD_001, HTTP 404")
    void ordenNoEncontrada() {
        OrdenNoValidaException ex = OrdenNoValidaException.noEncontrada(7L);
        assertEquals("ORD_001", ex.getCodigo());
        assertEquals(404, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("7"));
    }

    @Test
    @DisplayName("OrdenNoValidaException.maxItemsExcedido: código ORD_002, HTTP 409")
    void ordenMaxItems() {
        OrdenNoValidaException ex = OrdenNoValidaException.maxItemsExcedido(55, 50);
        assertEquals("ORD_002", ex.getCodigo());
        assertEquals(409, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("50"));
        assertTrue(ex.getMessage().contains("55"));
    }

    @Test
    @DisplayName("OrdenNoValidaException.transicionInvalida: código ORD_003, HTTP 409")
    void ordenTransicionInvalida() {
        OrdenNoValidaException ex = OrdenNoValidaException.transicionInvalida("PAID", "cancelar");
        assertEquals("ORD_003", ex.getCodigo());
        assertEquals(409, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("PAID"));
        assertTrue(ex.getMessage().contains("cancelar"));
    }

    // ── PagoFallidoException ──────────────────────────────────────────────────
    @Test
    @DisplayName("PagoFallidoException: código PAY_001, HTTP 422")
    void pagoFallido() {
        PagoFallidoException ex = new PagoFallidoException("Tarjeta");
        assertEquals("PAY_001", ex.getCodigo());
        assertEquals(422, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("Tarjeta"));
    }

    // ── AccesoDenegadoException ───────────────────────────────────────────────
    @Test
    @DisplayName("AccesoDenegadoException: código ACC_001, HTTP 403")
    void accesoDenegado() {
        AccesoDenegadoException ex = new AccesoDenegadoException("producto #5");
        assertEquals("ACC_001", ex.getCodigo());
        assertEquals(403, ex.getHttpStatus());
    }

    // ── ConfiguracionInvalidaException ────────────────────────────────────────
    @Test
    @DisplayName("ConfiguracionInvalidaException: código CFG_001, HTTP 400")
    void configuracionInvalida() {
        ConfiguracionInvalidaException ex = new ConfiguracionInvalidaException("IVA no puede ser negativo");
        assertEquals("CFG_001", ex.getCodigo());
        assertEquals(400, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("IVA"));
    }

    @Test
    @DisplayName("ConfiguracionInvalidaException.rfcCurpInvalido: código CFG_002, HTTP 400")
    void rfcCurpInvalido() {
        ConfiguracionInvalidaException ex = ConfiguracionInvalidaException.rfcCurpInvalido("Formato inválido");
        assertEquals("CFG_002", ex.getCodigo());
        assertEquals(400, ex.getHttpStatus());
    }
}

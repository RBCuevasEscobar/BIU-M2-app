package com.ecommerce;

import com.ecommerce.model.Carrito;
import com.ecommerce.model.ProductoFisico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del modelo Carrito — sin Spring.
 * Valida el patrón de sobrecarga de métodos agregarProducto().
 */
@DisplayName("Carrito — Modelo (Sobrecarga de Métodos)")
class CarritoModelTest {

    private Carrito carrito;
    private ProductoFisico laptop;
    private ProductoFisico teclado;

    @BeforeEach
    void setUp() {
        carrito = new Carrito();
        laptop  = new ProductoFisico("Laptop Pro", 25000.0, 10, 2.5);
        teclado = new ProductoFisico("Teclado Mecánico", 1500.0, 50, 0.8);
    }

    // ── SOBRECARGA 1: agregarProducto(Producto) ───────────────────────────────
    @Test
    @DisplayName("SOBRECARGA 1: agregarProducto(Producto) agrega un ítem")
    void sobrecarga1_agregarUnProducto() {
        carrito.agregarProducto(laptop);
        assertEquals(1, carrito.getProductos().size());
        assertTrue(carrito.getProductos().contains(laptop));
    }

    @Test
    @DisplayName("SOBRECARGA 1: MISMO producto agregado dos veces → 2 ítems en lista")
    void sobrecarga1_agregarMismoProductoDoVeces() {
        carrito.agregarProducto(laptop);
        carrito.agregarProducto(laptop);
        assertEquals(2, carrito.getProductos().size());
    }

    // ── SOBRECARGA 2: agregarProducto(Producto, int cantidad) ─────────────────
    @Test
    @DisplayName("SOBRECARGA 2: agregarProducto(Producto, 3) agrega 3 ítems")
    void sobrecarga2_agregarConCantidad() {
        carrito.agregarProducto(teclado, 3);
        assertEquals(3, carrito.getProductos().size());
    }

    @Test
    @DisplayName("SOBRECARGA 2: cantidad 0 → lista vacía")
    void sobrecarga2_cantidadCero() {
        carrito.agregarProducto(laptop, 0);
        assertTrue(carrito.getProductos().isEmpty());
    }

    // ── SOBRECARGA 3: agregarProducto(String nombre, Double precio) ───────────
    @Test
    @DisplayName("SOBRECARGA 3: agregarProducto(nombre, precio) crea ProductoFisico temporal")
    void sobrecarga3_agregarPorNombreYPrecio() {
        carrito.agregarProducto("Mouse Gamer", 800.0);
        assertEquals(1, carrito.getProductos().size());
        assertEquals("Mouse Gamer", carrito.getProductos().get(0).getNombre());
        assertEquals(800.0, carrito.getProductos().get(0).getPrecio(), 0.001);
    }

    // ── eliminarProducto ──────────────────────────────────────────────────────
    @Test
    @DisplayName("eliminarProducto: elimina la primera ocurrencia del producto")
    void eliminarProducto_eliminaUnaOcurrencia() {
        carrito.agregarProducto(laptop);
        carrito.agregarProducto(teclado);
        carrito.eliminarProducto(laptop);
        assertEquals(1, carrito.getProductos().size());
        assertFalse(carrito.getProductos().contains(laptop));
    }

    // ── getTotal ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("getTotal: suma los precios de todos los productos del carrito")
    void getTotal_calculaCorrectamente() {
        carrito.agregarProducto(laptop);   // 25000
        carrito.agregarProducto(teclado);  // 1500
        assertEquals(26500.0, carrito.getTotal(), 0.001);
    }

    @Test
    @DisplayName("getTotal: carrito vacío retorna 0.0")
    void getTotal_carritoVacio() {
        assertEquals(0.0, carrito.getTotal(), 0.001);
    }
}

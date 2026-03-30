package com.ecommerce;

import com.ecommerce.model.ProductoDigital;
import com.ecommerce.model.ProductoFisico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del modelo Producto y sus subclases.
 * Valida polimorfismo a través de mostrarDetalle() (sobreescritura)
 * y lógica de dominio específica de cada tipo.
 */
@DisplayName("Producto — Modelo (Polimorfismo / Overriding)")
class ProductoModelTest {

    // ── ProductoFisico ────────────────────────────────────────────────────────
    @Test
    @DisplayName("ProductoFisico.mostrarDetalle muestra nombre, precio, stock y peso")
    void productoFisico_mostrarDetalle() {
        ProductoFisico laptop = new ProductoFisico("Laptop Pro", 25000.0, 10, 2.5);
        String detalle = laptop.mostrarDetalle();
        assertTrue(detalle.contains("Laptop Pro"),  "Debe contener nombre");
        assertTrue(detalle.contains("25000"),       "Debe contener precio");
        assertTrue(detalle.contains("10"),          "Debe contener stock");
        assertTrue(detalle.contains("2.50"),        "Debe contener peso");
        assertTrue(detalle.contains("Físico"),      "Debe indicar tipo Físico");
    }

    @Test
    @DisplayName("ProductoFisico.tieneStock: stock > 0 → true")
    void productoFisico_tieneStock_conStock() {
        ProductoFisico pf = new ProductoFisico("Teclado", 500.0, 5, 0.5);
        assertTrue(pf.tieneStock());
    }

    @Test
    @DisplayName("ProductoFisico.tieneStock: stock = 0 → false")
    void productoFisico_tieneStock_sinStock() {
        ProductoFisico pf = new ProductoFisico("Sin Stock", 100.0, 0, 0.1);
        assertFalse(pf.tieneStock());
    }

    @Test
    @DisplayName("ProductoFisico.tieneStock: stock null → false")
    void productoFisico_tieneStock_stockNull() {
        ProductoFisico pf = new ProductoFisico();
        pf.setStock(null);
        assertFalse(pf.tieneStock());
    }

    @Test
    @DisplayName("ProductoFisico: getters retornan valores correctos del constructor")
    void productoFisico_getters() {
        ProductoFisico pf = new ProductoFisico("Monitor 4K", 8000.0, 3, 4.2);
        assertEquals("Monitor 4K", pf.getNombre());
        assertEquals(8000.0, pf.getPrecio(), 0.001);
        assertEquals(3, pf.getStock());
        assertEquals(4.2, pf.getPeso(), 0.001);
    }

    // ── ProductoDigital ───────────────────────────────────────────────────────
    @Test
    @DisplayName("ProductoDigital.mostrarDetalle muestra nombre, precio y URL")
    void productoDigital_mostrarDetalle() {
        ProductoDigital pd = new ProductoDigital();
        pd.setNombre("Ebook Java");
        pd.setPrecio(299.99);
        pd.setUrlDescarga("https://cdn.example.com/ebook.pdf");
        String detalle = pd.mostrarDetalle();
        assertTrue(detalle.contains("Ebook Java"),  "Debe contener nombre");
        assertTrue(detalle.contains("299"),         "Debe contener precio");
        assertTrue(detalle.contains("https://"),    "Debe contener URL");
    }

    // ── Polimorfismo ──────────────────────────────────────────────────────────
    @Test
    @DisplayName("Polimorfismo: ProductoFisico y ProductoDigital muestran detalle diferente")
    void polimorfismo_mostrarDetalleDiferente() {
        ProductoFisico  fisico  = new ProductoFisico("Laptop", 20000.0, 5, 2.0);
        ProductoDigital digital = new ProductoDigital();
        digital.setNombre("Curso Online");
        digital.setPrecio(499.0);
        digital.setUrlDescarga("https://curso.com");

        // El método es el mismo (mostrarDetalle) pero la implementación es distinta
        assertNotEquals(fisico.mostrarDetalle(), digital.mostrarDetalle());
        assertTrue(fisico.mostrarDetalle().contains("Físico"));
        assertTrue(digital.mostrarDetalle().contains("Digital"));
    }
}

package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa el carrito de compras de un usuario.
 *
 * ═══════════════════════════════════════════════════════════════
 * CONCEPTO OOP: SOBRECARGA DE MÉTODOS (Method Overloading)
 * ═══════════════════════════════════════════════════════════════
 * Esta clase demuestra SOBRECARGA a nivel de dominio.
 * Los tres métodos agregarProducto() tienen el MISMO nombre pero
 * DIFERENTES parámetros. Java distingue cuál usar en tiempo de
 * compilación según el tipo/número de argumentos recibidos.
 *
 * Firmas sobrecargadas:
 * 1. agregarProducto(Producto producto) → versión principal
 * 2. agregarProducto(Producto producto, int cantidad) → con cantidad
 * 3. agregarProducto(String nombre, Double precio) → con datos inline
 *
 * Todos delegan internamente al método principal, manteniendo
 * compatibilidad con el código existente.
 * ═══════════════════════════════════════════════════════════════
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "carritos")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Relación con Producto (ManyToMany para simplicidad en Fase 1, permitiendo
    // duplicados para cantidad)
    @ManyToMany
    @JoinTable(name = "carrito_productos", joinColumns = @JoinColumn(name = "carrito_id"), inverseJoinColumns = @JoinColumn(name = "producto_id"))
    private List<Producto> productos = new ArrayList<>();

    // ─────────────────────────────────────────────────
    // SOBRECARGA 1: Método principal (existente, sin cambios)
    // Agrega un producto individual al carrito.
    // ─────────────────────────────────────────────────
    public void agregarProducto(Producto producto) {
        this.productos.add(producto);
    }

    // ─────────────────────────────────────────────────
    // SOBRECARGA 2: Agrega el mismo producto N veces.
    // Mismo nombre, diferente firma: (Producto, int).
    // Delega internamente al método principal (SOBRECARGA 1)
    // para respetar el principio DRY.
    // ─────────────────────────────────────────────────
    public void agregarProducto(Producto producto, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            agregarProducto(producto); // Delegación al método principal
        }
    }

    // ─────────────────────────────────────────────────
    // SOBRECARGA 3: Crea un ProductoFisico genérico con
    // nombre y precio, y lo agrega al carrito.
    // Útil para escenarios de prueba o productos temporales.
    // Mismo nombre, diferente firma: (String, Double).
    // También delega al método principal (SOBRECARGA 1).
    // ─────────────────────────────────────────────────
    public void agregarProducto(String nombre, Double precio) {
        ProductoFisico productoTemp = new ProductoFisico();
        productoTemp.setNombre(nombre);
        productoTemp.setPrecio(precio);
        productoTemp.setStock(0);
        productoTemp.setPeso(0.0);
        agregarProducto(productoTemp); // Delegación al método principal
    }

    public void eliminarProducto(Producto producto) {
        this.productos.remove(producto);
    }

    public Double getTotal() {
        return productos.stream().mapToDouble(Producto::getPrecio).sum();
    }
}

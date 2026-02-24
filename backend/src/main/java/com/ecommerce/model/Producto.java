package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase base abstracta para todos los tipos de producto.
 *
 * ═══════════════════════════════════════════════════════════════
 * CONCEPTO OOP: ABSTRACCIÓN + SOBREESCRITURA (Method Overriding)
 * ═══════════════════════════════════════════════════════════════
 * Al ser abstracta, Producto define el CONTRATO que toda subclase
 * debe cumplir: el método mostrarDetalle() es abstracto, por lo
 * que CADA subclase está obligada a sobreescribirlo con su
 * propia implementación específica.
 *
 * Jerarquía:
 * Producto (abstracta)
 * ├── ProductoFisico → @Override mostrarDetalle() → información de stock/peso
 * └── ProductoDigital → @Override mostrarDetalle() → URL de descarga/vigencia
 *
 * Polimorfismo en acción:
 * List<Producto> lista = Arrays.asList(
 * new ProductoFisico("Laptop", 25000.0, 5, 2.5),
 * new ProductoDigital("Ebook", 299.99, "http://...")
 * );
 * lista.forEach(p -> System.out.println(p.mostrarDetalle()));
 * // Cada elemento imprime su propio detalle específico
 * ═══════════════════════════════════════════════════════════════
 */
@Data
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "productos")
public abstract class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    // Constructor parametrizado (usado por subclases via super(...))
    public Producto(String nombre, Double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    /**
     * Método abstracto que debe ser SOBREESCRITO por cada subclase.
     *
     * Contrato: retorna una descripción textual del producto con
     * la información más relevante según su tipo concreto.
     *
     * @return Cadena con el detalle específico del tipo de producto.
     */
    public abstract String mostrarDetalle();
}

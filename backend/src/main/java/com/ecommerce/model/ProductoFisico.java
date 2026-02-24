package com.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Representa un producto de tipo físico (tiene stock y peso).
 *
 * ═══════════════════════════════════════════════════════════════
 * CONCEPTO OOP: SOBREESCRITURA DE MÉTODOS (Method Overriding)
 * ═══════════════════════════════════════════════════════════════
 * Esta clase SOBREESCRIBE el método abstracto mostrarDetalle()
 * definido en la clase padre {@link Producto}.
 *
 * La sobreescritura permite que cada tipo de producto devuelva
 * información ESPECÍFICA de su propia naturaleza, aunque todos
 * sean tratados polimórficamente como un {@code Producto}.
 *
 * Reglas de sobreescritura respetadas:
 * - Misma firma: public String mostrarDetalle()
 * - Mismo tipo de retorno: String
 * - Anotación @Override garantiza corrección en compilación
 * - Acceso igual o más permisivo que la clase padre (public)
 * ═══════════════════════════════════════════════════════════════
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "productos_fisicos")
public class ProductoFisico extends Producto {

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Double peso;

    // Constructor parametrizado
    public ProductoFisico(String nombre, Double precio, Integer stock, Double peso) {
        super(nombre, precio);
        this.stock = stock;
        this.peso = peso;
    }

    // ─────────────────────────────────────────────────────────────
    // SOBREESCRITURA del método abstracto de Producto.
    //
    // @Override indica explícitamente que este método reemplaza
    // al de la clase padre. Si el nombre estuviera mal escrito,
    // el compilador lo detectaría como error.
    //
    // Esta implementación aporta información ESPECÍFICA de un
    // producto físico: stock disponible y peso en kg.
    // ─────────────────────────────────────────────────────────────
    @Override
    public String mostrarDetalle() {
        return String.format(
                "[Físico] %s | Precio: $%.2f | Stock: %d unidades | Peso: %.2f kg",
                getNombre(), getPrecio(), stock, peso);
    }

    /**
     * Indica si el producto tiene stock disponible.
     * Información de dominio específica de ProductoFisico.
     */
    public boolean tieneStock() {
        return stock != null && stock > 0;
    }
}

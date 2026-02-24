package com.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Representa un producto de tipo digital (descargable).
 *
 * ═══════════════════════════════════════════════════════════════
 * CONCEPTO OOP: SOBREESCRITURA DE MÉTODOS (Method Overriding)
 * ═══════════════════════════════════════════════════════════════
 * Esta clase también SOBREESCRIBE mostrarDetalle() definido en
 * {@link Producto}, pero con información DIFERENTE a la de
 * {@link ProductoFisico}.
 *
 * Esto ilustra el POLIMORFISMO de sobreescritura: el mismo
 * mensaje (mostrarDetalle()) produce resultados distintos
 * dependiendo del tipo concreto del objeto en tiempo de ejecución.
 *
 * Ejemplo de polimorfismo:
 * Producto p1 = new ProductoFisico(...);
 * Producto p2 = new ProductoDigital(...);
 * p1.mostrarDetalle() → "[Físico] ..."
 * p2.mostrarDetalle() → "[Digital] ..."
 * ═══════════════════════════════════════════════════════════════
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "productos_digitales")
public class ProductoDigital extends Producto {

    @Column(name = "url_descarga", nullable = false)
    private String urlDescarga;

    @Column(name = "dias_expiracion")
    private Integer diasExpiracion;

    // Constructor básico (compatible con código existente)
    public ProductoDigital(String nombre, Double precio, String urlDescarga) {
        super(nombre, precio);
        this.urlDescarga = urlDescarga;
        this.diasExpiracion = null; // Sin expiración por defecto
    }

    // Constructor completo con días de expiración
    public ProductoDigital(String nombre, Double precio, String urlDescarga, Integer diasExpiracion) {
        super(nombre, precio);
        this.urlDescarga = urlDescarga;
        this.diasExpiracion = diasExpiracion;
    }

    // ─────────────────────────────────────────────────────────────
    // SOBREESCRITURA del método abstracto de Producto.
    //
    // A diferencia de ProductoFisico, aquí se muestra informacion
    // relevante para un producto digital: URL de descarga y
    // vigencia del acceso.
    //
    // Mismo contrato (firma + tipo de retorno), diferente
    // implementación → POLIMORFISMO por sobreescritura.
    // ─────────────────────────────────────────────────────────────
    @Override
    public String mostrarDetalle() {
        String expiracion = (diasExpiracion != null)
                ? diasExpiracion + " días"
                : "Sin expiración";

        return String.format(
                "[Digital] %s | Precio: $%.2f | URL: %s | Vigencia: %s",
                getNombre(), getPrecio(), urlDescarga, expiracion);
    }

    /**
     * Indica si el producto tiene un período de vigencia limitado.
     */
    public boolean tieneExpiracion() {
        return diasExpiracion != null && diasExpiracion > 0;
    }
}

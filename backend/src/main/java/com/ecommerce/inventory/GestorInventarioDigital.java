package com.ecommerce.inventory;

import com.ecommerce.model.Producto;
import com.ecommerce.model.ProductoDigital;
import org.springframework.stereotype.Component;

/**
 * Implementación de Inventario para Productos Digitales.
 * Los productos digitales tienen stock ilimitado infinito en cuanto a venta.
 */
@Component
public class GestorInventarioDigital extends GestorInventario {

    @Override
    public boolean verificarStock(Producto producto, int cantidad) {
        // Los productos digitales siempre tienen stock disponible en nuestra lógica de
        // negocio
        if (producto instanceof ProductoDigital) {
            return true;
        }
        throw new IllegalArgumentException("El producto debe ser digital.");
    }

    @Override
    public void actualizarStock(Producto producto, int cantidadDiferencia) {
        // Nada que hacer para digitales porque no manejan inventario contable.
        // Se deja vacío a propósito demostrando polimorfismo seguro.
    }
}

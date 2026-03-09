package com.ecommerce.inventory;

import com.ecommerce.model.Producto;

/**
 * Clase abstracta que define el contrato para la gestión de inventario.
 * Demuestra los principios OOP de Abstracción y Polimorfismo.
 */
public abstract class GestorInventario {

    /**
     * Verifica si hay stock suficiente para un producto.
     * 
     * @param producto el producto a verificar
     * @param cantidad la cantidad requerida
     * @return true si hay disponibilidad, false de lo contrario
     */
    public abstract boolean verificarStock(Producto producto, int cantidad);

    /**
     * Actualiza el stock de un producto. Positivo para añadir, negativo para
     * descontar.
     * 
     * @param producto           el producto cuyo stock se modificará
     * @param cantidadDiferencia la diferencia a aplicar
     */
    public abstract void actualizarStock(Producto producto, int cantidadDiferencia);

}

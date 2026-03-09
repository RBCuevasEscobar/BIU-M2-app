package com.ecommerce.inventory;

import com.ecommerce.model.Producto;
import com.ecommerce.model.ProductoFisico;
import org.springframework.stereotype.Component;

/**
 * Implementación de Inventario para Productos Físicos.
 * El stock decae al confirmar un pago de orden.
 */
@Component
public class GestorInventarioFisico extends GestorInventario {

    @Override
    public boolean verificarStock(Producto producto, int cantidad) {
        if (producto instanceof ProductoFisico) {
            ProductoFisico pf = (ProductoFisico) producto;
            return pf.getStock() != null && pf.getStock() >= cantidad;
        }
        throw new IllegalArgumentException("El producto debe ser físico.");
    }

    @Override
    public void actualizarStock(Producto producto, int cantidadDiferencia) {
        if (producto instanceof ProductoFisico) {
            ProductoFisico pf = (ProductoFisico) producto;
            int stockActual = pf.getStock() != null ? pf.getStock() : 0;
            pf.setStock(stockActual + cantidadDiferencia);
        } else {
            throw new IllegalArgumentException("El producto debe ser físico.");
        }
    }
}

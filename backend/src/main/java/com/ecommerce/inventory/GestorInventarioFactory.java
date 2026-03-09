package com.ecommerce.inventory;

import com.ecommerce.model.Producto;
import com.ecommerce.model.ProductoFisico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Factory (o Adapter) para obtener el Gestor de Inventario adecuado
 * según el polimorfismo del Producto.
 */
@Service
public class GestorInventarioFactory {

    @Autowired
    private GestorInventarioFisico gestorFisico;

    @Autowired
    private GestorInventarioDigital gestorDigital;

    public GestorInventario obtenerGestor(Producto producto) {
        if (producto instanceof ProductoFisico) {
            return gestorFisico;
        }
        return gestorDigital;
    }
}

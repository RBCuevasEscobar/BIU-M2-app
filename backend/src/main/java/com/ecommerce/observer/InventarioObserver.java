package com.ecommerce.observer;

import com.ecommerce.config.ConfiguracionSistema;
import com.ecommerce.inventory.GestorInventario;
import com.ecommerce.inventory.GestorInventarioFactory;
import com.ecommerce.model.OrdenDetalle;
import com.ecommerce.model.ProductoFisico;
import com.ecommerce.observer.events.OrdenPagadaEvent;
import com.ecommerce.observer.events.StockBajoEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observador encargado de rectificar el desabasto y delegar el Inventario.
 */
@Component
public class InventarioObserver {

    @Autowired
    private GestorInventarioFactory inventarioFactory;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onOrdenPagada(OrdenPagadaEvent event) {
        System.out.println("[InventarioObserver] Procesando rectificación de stock para Orden ID: " + event.getOrden().getId());

        int stockMinimo = ConfiguracionSistema.getInstance().getStockMinimo();

        for (OrdenDetalle detalle : event.getOrden().getDetalles()) {
            GestorInventario gestor = inventarioFactory.obtenerGestor(detalle.getProducto());
            gestor.actualizarStock(detalle.getProducto(), -detalle.getCantidad());

            if (detalle.getProducto() instanceof ProductoFisico pf) {
                if (pf.getStock() <= stockMinimo) {
                    eventPublisher.publishEvent(new StockBajoEvent(this, pf, pf.getStock()));
                }
            }
        }
    }
}

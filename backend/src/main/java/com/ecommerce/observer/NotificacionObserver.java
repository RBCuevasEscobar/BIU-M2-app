package com.ecommerce.observer;

import com.ecommerce.observer.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observador pasivo que actua como Sender de e-mails y notificaciones SMS al Cliente.
 */
@Component
public class NotificacionObserver {

    @EventListener
    public void handleOrdenCreada(OrdenCreadaEvent event) {
        System.out.println("[NotificacionObserver] EMAIL ENVIADO: Tu orden #" + event.getOrden().getId() + " ha sido recibida y está en estado CREATED.");
    }

    @EventListener
    public void handleOrdenPagada(OrdenPagadaEvent event) {
        System.out.println("[NotificacionObserver] SMS ENVIADO: El pago para la orden #" + event.getOrden().getId() + " ha sido procesado exitosamente.");
    }

    @EventListener
    public void handleOrdenCancelada(OrdenCanceladaEvent event) {
        System.out.println("[NotificacionObserver] EMAIL ENVIADO: Aviso de cancelación para la orden #" + event.getOrden().getId() + ".");
    }

    @EventListener
    public void handleOrdenDespachada(OrdenDespachadaEvent event) {
        System.out.println("[NotificacionObserver] EMAIL ENVIADO: Tu orden #" + event.getOrden().getId() + " va en camino. Rastreo: " + event.getOrden().getShipment().getTrackingNumber());
    }

    @EventListener
    public void handleOrdenEntregada(OrdenEntregadaEvent event) {
        System.out.println("[NotificacionObserver] SMS ENVIADO: Paquete de la orden #" + event.getOrden().getId() + " entregado. ¡Gracias por tu compra!");
    }

    @EventListener
    public void handleStockBajo(StockBajoEvent event) {
        System.out.println("[NotificacionObserver] ALERTA ADMINISTRADOR: El producto '" + event.getProducto().getNombre() + "' tiene un nivel crítico de stock (" + event.getSaldoActual() + " restantes).");
    }
}

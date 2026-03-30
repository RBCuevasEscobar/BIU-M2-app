package com.ecommerce.observer;

import com.ecommerce.model.NotificacionPendiente;
import com.ecommerce.observer.events.*;
import com.ecommerce.repository.NotificacionPendienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observador pasivo que actua como Sender de e-mails y notificaciones SMS al Cliente.
 * También persiste notificaciones para el ADMIN en la cola de pendientes.
 */
@Component
public class NotificacionObserver {

    @Autowired
    private NotificacionPendienteRepository notificacionRepo;

    @EventListener
    public void handleOrdenCreada(OrdenCreadaEvent event) {
        System.out.println("[NotificacionObserver] EMAIL ENVIADO: Tu orden #" + event.getOrden().getId() + " ha sido recibida y está en estado CREATED.");
    }

    @EventListener
    public void handleOrdenPagada(OrdenPagadaEvent event) {
        System.out.println("[NotificacionObserver] SMS ENVIADO: El pago para la orden #" + event.getOrden().getId() + " ha sido procesado exitosamente.");
    }

    @EventListener
    public void handleOrdenPagoPendiente(OrdenPagoPendienteEvent event) {
        System.out.println("[NotificacionObserver] EMAIL ENVIADO: Tu orden #" + event.getOrden().getId() + " se encuentra pendiente de pago.");
    }

    @EventListener
    public void handleOrdenSinStock(OrdenSinStockEvent event) {
        System.out.println("[NotificacionObserver] EMAIL ENVIADO: Reembolso efectuado. Tu orden #" + event.getOrden().getId() + " ha sido cancelada por escasez de stock.");
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
        String msg = "[ALERTA STOCK] El producto '" + event.getProducto().getNombre()
                + "' ha alcanzado el nivel mínimo de stock (" + event.getSaldoActual() + " unidades restantes).";
        System.out.println("[NotificacionObserver] ALERTA ADMINISTRADOR: " + msg);
        // Persistir en cola de notificaciones pendientes para ADMIN
        notificacionRepo.save(new NotificacionPendiente(msg));
    }

    @EventListener
    public void handleMaxItemsExcedido(MaxItemsExcedidoEvent event) {
        String msg = "[ALERTA PEDIDO] El usuario '" + event.getUsuario().getNombre()
                + "' intentó crear una orden con " + event.getCantidadIntentada()
                + " ítems, superando el límite configurado de " + event.getMaxPermitido() + ".";
        System.out.println("[NotificacionObserver] ALERTA ADMINISTRADOR: " + msg);
        // Persistir en cola de notificaciones pendientes para ADMIN
        notificacionRepo.save(new NotificacionPendiente(msg));
    }
}

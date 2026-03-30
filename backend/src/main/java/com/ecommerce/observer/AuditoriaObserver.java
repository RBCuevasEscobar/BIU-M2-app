package com.ecommerce.observer;

import com.ecommerce.observer.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Observador asíncrono que escribe en logs de auditoría sin bloquear el thread de la transacción principal.
 */
@Component
public class AuditoriaObserver {

    @Async
    @EventListener
    public void auditarTransicionPagada(OrdenPagadaEvent event) {
        System.out.println("[AZURE_LOG_TO_REMOVE] [AUDIT LOG] Transacción de pago procesada por $" + event.getOrden().getTotal() + " en Orden ID " + event.getOrden().getId());
    }

    @Async
    @EventListener
    public void auditarTransicionCancelada(OrdenCanceladaEvent event) {
        System.out.println("[AZURE_LOG_TO_REMOVE] [AUDIT LOG] Usuario " + event.getOrden().getUsuario().getEmail() + " ha cancelado la orden " + event.getOrden().getId());
    }
    
    @Async
    @EventListener
    public void auditarDespacho(OrdenDespachadaEvent event) {
        System.out.println("[AZURE_LOG_TO_REMOVE] [AUDIT LOG] Orden " + event.getOrden().getId() + " ha entrado a carrier " + event.getOrden().getShipment().getCourier());
    }

    @Async
    @EventListener
    public void auditarOrdenCreada(OrdenCreadaEvent event) {
        System.out.println("[AZURE_LOG_TO_REMOVE] [AUDIT LOG] Transacción de creación: Usuario " + event.getOrden().getUsuario().getEmail() + " creó Orden ID " + event.getOrden().getId());
    }

    @Async
    @EventListener
    public void auditarOrdenPagoPendiente(OrdenPagoPendienteEvent event) {
        System.out.println("[AZURE_LOG_TO_REMOVE] [AUDIT LOG] Transacción pendiente: Orden ID " + event.getOrden().getId() + " esperando cobro");
    }

    @Async
    @EventListener
    public void auditarOrdenSinStock(OrdenSinStockEvent event) {
        System.out.println("[AZURE_LOG_TO_REMOVE] [AUDIT LOG] Falla transaccional: Orden ID " + event.getOrden().getId() + " rebotada por falta de stock");
    }

    @Async
    @EventListener
    public void auditarOrdenEntregada(OrdenEntregadaEvent event) {
        System.out.println("[AZURE_LOG_TO_REMOVE] [AUDIT LOG] Transacción finalizada: Orden ID " + event.getOrden().getId() + " entregada al cliente");
    }
}

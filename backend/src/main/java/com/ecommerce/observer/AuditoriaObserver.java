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
        System.out.println("[AuditoriaObserver-Async] AUDIT LOG: Transacción de pago procesada por $" + event.getOrden().getTotal() + " en Orden ID " + event.getOrden().getId());
    }

    @Async
    @EventListener
    public void auditarTransicionCancelada(OrdenCanceladaEvent event) {
        System.out.println("[AuditoriaObserver-Async] AUDIT LOG: Usuario " + event.getOrden().getUsuario().getEmail() + " ha cancelado la orden " + event.getOrden().getId());
    }
    
    @Async
    @EventListener
    public void auditarDespacho(OrdenDespachadaEvent event) {
        System.out.println("[AuditoriaObserver-Async] AUDIT LOG: Orden " + event.getOrden().getId() + " ha entrado a carrier " + event.getOrden().getShipment().getCourier());
    }
}

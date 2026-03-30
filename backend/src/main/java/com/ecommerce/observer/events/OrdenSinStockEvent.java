package com.ecommerce.observer.events;

import com.ecommerce.model.Orden;
import org.springframework.context.ApplicationEvent;

public class OrdenSinStockEvent extends ApplicationEvent {
    private final Orden orden;

    public OrdenSinStockEvent(Object source, Orden orden) {
        super(source);
        this.orden = orden;
    }

    public Orden getOrden() {
        return orden;
    }
}

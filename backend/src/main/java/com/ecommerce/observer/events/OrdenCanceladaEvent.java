package com.ecommerce.observer.events;

import com.ecommerce.model.Orden;
import org.springframework.context.ApplicationEvent;

public class OrdenCanceladaEvent extends ApplicationEvent {
    private final Orden orden;

    public OrdenCanceladaEvent(Object source, Orden orden) {
        super(source);
        this.orden = orden;
    }

    public Orden getOrden() {
        return orden;
    }
}

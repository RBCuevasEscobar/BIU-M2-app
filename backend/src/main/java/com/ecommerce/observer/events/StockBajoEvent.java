package com.ecommerce.observer.events;

import com.ecommerce.model.Producto;
import org.springframework.context.ApplicationEvent;

public class StockBajoEvent extends ApplicationEvent {
    private final Producto producto;
    private final int saldoActual;

    public StockBajoEvent(Object source, Producto producto, int saldoActual) {
        super(source);
        this.producto = producto;
        this.saldoActual = saldoActual;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getSaldoActual() {
        return saldoActual;
    }
}

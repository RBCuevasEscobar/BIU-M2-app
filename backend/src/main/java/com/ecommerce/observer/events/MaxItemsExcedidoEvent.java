package com.ecommerce.observer.events;

import com.ecommerce.model.Usuario;
import org.springframework.context.ApplicationEvent;

/**
 * Evento que se publica cuando un CUSTOMER intenta crear una orden
 * con más productos de los permitidos por la configuración del sistema.
 */
public class MaxItemsExcedidoEvent extends ApplicationEvent {

    private final Usuario usuario;
    private final int cantidadIntentada;
    private final int maxPermitido;

    public MaxItemsExcedidoEvent(Object source, Usuario usuario, int cantidadIntentada, int maxPermitido) {
        super(source);
        this.usuario = usuario;
        this.cantidadIntentada = cantidadIntentada;
        this.maxPermitido = maxPermitido;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public int getCantidadIntentada() {
        return cantidadIntentada;
    }

    public int getMaxPermitido() {
        return maxPermitido;
    }
}

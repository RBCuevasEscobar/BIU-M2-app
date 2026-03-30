package com.ecommerce.exception;

/**
 * Se lanza cuando no hay suficiente inventario para completar el pago de una orden.
 * El estado de la orden se actualiza a OUT_OF_STOCK antes de lanzar esta excepción.
 * HTTP 409 Conflict.
 * Código: INV_001
 */
public class InventarioInsuficienteException extends EcommerceException {

    public InventarioInsuficienteException(String nombreProducto, int disponible, int solicitado) {
        super("INV_001", 409,
                String.format("Stock insuficiente para '%s'. Disponible: %d, Solicitado: %d",
                        nombreProducto, disponible, solicitado));
    }

    public InventarioInsuficienteException(String mensaje) {
        super("INV_001", 409, mensaje);
    }
}

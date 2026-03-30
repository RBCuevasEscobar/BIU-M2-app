package com.ecommerce.exception;

/**
 * Se lanza cuando el carrito del usuario está vacío al intentar crear una orden.
 * HTTP 400 Bad Request.
 * Código: CART_001
 */
public class CarritoVacioException extends EcommerceException {

    public CarritoVacioException() {
        super("CART_001", 400, "El carrito está vacío. Agrega productos antes de continuar.");
    }

    public CarritoVacioException(String mensaje) {
        super("CART_001", 400, mensaje);
    }
}

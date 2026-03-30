package com.ecommerce.exception;

/**
 * Se lanza cuando el procesador de pago no puede confirmar el pago de la orden.
 * HTTP 422 Unprocessable Entity.
 * Código: PAY_001
 */
public class PagoFallidoException extends EcommerceException {

    public PagoFallidoException(String metodoPago) {
        super("PAY_001", 422,
                String.format("El pago mediante '%s' no pudo procesarse. Intenta de nuevo.", metodoPago));
    }

    public PagoFallidoException(String metodoPago, String detalle) {
        super("PAY_001", 422,
                String.format("Pago fallido [%s]: %s", metodoPago, detalle));
    }
}

package com.ecommerce.model;

/**
 * Enumeración que representa los posibles estados del ciclo de vida de una
 * Orden.
 */
public enum EstadoOrden {
    CREATED, // Orden generada desde el carrito
    PAYMENT_PENDING, // La orden requiere pago o el pago anterior no fue exitoso
    PAID, // El pago fue procesado exitosamente
    OUT_OF_STOCK, // Uno o más productos no tienen stock
    CANCELLED, // La orden fue cancelada
    SHIPPED, // El pedido fue despachado
    DELIVERED // El pedido fue entregado
}

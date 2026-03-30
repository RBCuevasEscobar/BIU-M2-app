package com.ecommerce.exception;

/**
 * Se lanza cuando la orden no puede avanzar al estado siguiente
 * por una violación de la máquina de estados del ciclo de la orden,
 * o cuando los parámetros de la orden son inválidos (ej: MAX_ITEMS excedido).
 *
 * Códigos:
 *  ORD_001 — orden no encontrada
 *  ORD_002 — excede el máximo de ítems por orden
 *  ORD_003 — transición de estado inválida (ej: ya fue pagada/cancelada)
 *  ORD_004 — dirección de envío no válida
 *  ORD_005 — orden sin propietario válido
 *
 * HTTP 409 Conflict (por defecto para conflictos de estado).
 */
public class OrdenNoValidaException extends EcommerceException {

    public OrdenNoValidaException(String codigo, int httpStatus, String mensaje) {
        super(codigo, httpStatus, mensaje);
    }

    /** Orden no encontrada por ID */
    public static OrdenNoValidaException noEncontrada(Long id) {
        return new OrdenNoValidaException("ORD_001", 404, "Orden no encontrada con id: " + id);
    }

    /** Carrito supera el máximo de ítems configurado en el Singleton */
    public static OrdenNoValidaException maxItemsExcedido(int total, int maximo) {
        return new OrdenNoValidaException("ORD_002", 409,
                String.format("El carrito excede el límite de %d ítems por orden. Total actual: %d", maximo, total));
    }

    /** El estado de la orden no permite la operación solicitada */
    public static OrdenNoValidaException transicionInvalida(String estadoActual, String operacion) {
        return new OrdenNoValidaException("ORD_003", 409,
                String.format("La operación '%s' no está permitida para una orden en estado '%s'",
                        operacion, estadoActual));
    }

    /** La dirección de envío no pertenece al usuario propietario de la orden */
    public static OrdenNoValidaException direccionInvalida() {
        return new OrdenNoValidaException("ORD_004", 400,
                "La dirección de envío seleccionada no pertenece al propietario de la orden.");
    }
}

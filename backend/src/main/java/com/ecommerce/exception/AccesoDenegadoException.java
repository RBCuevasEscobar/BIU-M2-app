package com.ecommerce.exception;

/**
 * Se lanza cuando un usuario intenta realizar una operación sobre un recurso
 * que no le pertenece o que requiere un rol superior.
 *
 * Complementa la seguridad JWT: el JWT garantiza autenticación;
 * esta excepción maneja autorización a nivel de negocio (ej: editar
 * un producto que no es tuyo).
 *
 * HTTP 403 Forbidden.
 * Código: ACC_001
 */
public class AccesoDenegadoException extends EcommerceException {

    public AccesoDenegadoException() {
        super("ACC_001", 403, "No tienes permisos para realizar esta operación.");
    }

    public AccesoDenegadoException(String recurso) {
        super("ACC_001", 403, "Acceso denegado al recurso: " + recurso);
    }

    public AccesoDenegadoException(String codigo, String mensaje) {
        super(codigo, 403, mensaje);
    }
}

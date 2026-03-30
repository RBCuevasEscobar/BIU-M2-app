package com.ecommerce.exception;

/**
 * Se lanza cuando no se encuentra un usuario por ID o email.
 * HTTP 404 Not Found.
 * Códigos:
 *  USR_001 — usuario no encontrado por ID o email
 *  USR_002 — email ya registrado (al crear)
 *  USR_003 — credenciales inválidas (al hacer login)
 *  USR_004 — RFC/CURP inválido o faltante
 */
public class UsuarioNoEncontradoException extends EcommerceException {

    public UsuarioNoEncontradoException(Long id) {
        super("USR_001", 404, "Usuario no encontrado con id: " + id);
    }

    public UsuarioNoEncontradoException(String email) {
        super("USR_001", 404, "Usuario no encontrado: " + email);
    }

    public UsuarioNoEncontradoException(String codigo, int httpStatus, String mensaje) {
        super(codigo, httpStatus, mensaje);
    }

    /** Para usar cuando el email ya está registrado (código USR_002, HTTP 409) */
    public static UsuarioNoEncontradoException emailDuplicado(String email) {
        return new UsuarioNoEncontradoException("USR_002", 409, "El email ya está registrado: " + email);
    }

    /** Para usar cuando las credenciales son inválidas (código USR_003, HTTP 401) */
    public static UsuarioNoEncontradoException credencialesInvalidas() {
        return new UsuarioNoEncontradoException("USR_003", 401, "Credenciales inválidas");
    }

    /** Para RFC/CURP inválido o faltante (código USR_004, HTTP 400) */
    public static UsuarioNoEncontradoException rfcInvalido(String mensaje) {
        return new UsuarioNoEncontradoException("USR_004", 400, mensaje);
    }
}

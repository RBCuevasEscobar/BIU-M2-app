package com.ecommerce.exception;

/**
 * Se lanza cuando se recibe un valor inválido en los parámetros de
 * configuración del sistema (IVA, moneda, stock mínimo, max ítems)
 * o cuando RFC/CURP de un cliente no cumple el formato requerido.
 *
 * Códigos:
 *  CFG_001 — parámetro de sistema inválido (IVA, maxItems, etc.)
 *  CFG_002 — RFC/CURP inválido (formato o fecha no coincide)
 *
 * HTTP 400 Bad Request.
 */
public class ConfiguracionInvalidaException extends EcommerceException {

    public ConfiguracionInvalidaException(String mensaje) {
        super("CFG_001", 400, mensaje);
    }

    public ConfiguracionInvalidaException(String codigo, String mensaje) {
        super(codigo, 400, mensaje);
    }

    /** RFC/CURP inválido o no coincide con fecha de nacimiento */
    public static ConfiguracionInvalidaException rfcCurpInvalido(String detalle) {
        return new ConfiguracionInvalidaException("CFG_002", detalle);
    }
}

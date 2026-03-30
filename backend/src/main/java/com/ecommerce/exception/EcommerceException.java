package com.ecommerce.exception;

/**
 * Clase base de todas las excepciones de dominio del sistema eCommerce.
 *
 * Extiende RuntimeException para ser unchecked — los services no necesitan
 * declarar throws ni los controllers capturarlas manualmente; el
 * GlobalExceptionHandler las intercepta automáticamente.
 *
 * Cada excepción concreta define:
 *  - codigo   : identificador único de error (ej. "PROD_001")
 *  - httpStatus: código HTTP que el GlobalExceptionHandler usará en la respuesta
 */
public abstract class EcommerceException extends RuntimeException {

    private final String codigo;
    private final int    httpStatus;

    protected EcommerceException(String codigo, int httpStatus, String mensaje) {
        super(mensaje);
        this.codigo     = codigo;
        this.httpStatus = httpStatus;
    }

    protected EcommerceException(String codigo, int httpStatus, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo     = codigo;
        this.httpStatus = httpStatus;
    }

    public String getCodigo()     { return codigo; }
    public int    getHttpStatus() { return httpStatus; }
}

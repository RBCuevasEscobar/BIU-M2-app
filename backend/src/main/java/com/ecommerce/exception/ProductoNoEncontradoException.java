package com.ecommerce.exception;

/**
 * Se lanza cuando no se encuentra un producto por su ID en la base de datos.
 * HTTP 404 Not Found.
 * Código: PROD_001
 */
public class ProductoNoEncontradoException extends EcommerceException {

    public ProductoNoEncontradoException(Long id) {
        super("PROD_001", 404, "Producto no encontrado con id: " + id);
    }

    public ProductoNoEncontradoException(String mensaje) {
        super("PROD_001", 404, mensaje);
    }
}

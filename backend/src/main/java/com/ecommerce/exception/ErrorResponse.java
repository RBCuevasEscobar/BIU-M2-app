package com.ecommerce.exception;

import java.time.LocalDateTime;

/**
 * DTO de respuesta estándar para todos los errores del sistema.
 *
 * Formato JSON generado:
 * {
 *   "timestamp": "2026-03-28T20:00:00",
 *   "codigo":    "PROD_001",
 *   "mensaje":   "Producto no encontrado con id: 5",
 *   "path":      "/api/productos/5"
 * }
 */
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final String        codigo;
    private final String        mensaje;
    private final String        path;

    public ErrorResponse(String codigo, String mensaje, String path) {
        this.timestamp = LocalDateTime.now();
        this.codigo    = codigo;
        this.mensaje   = mensaje;
        this.path      = path;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String        getCodigo()    { return codigo; }
    public String        getMensaje()   { return mensaje; }
    public String        getPath()      { return path; }
}

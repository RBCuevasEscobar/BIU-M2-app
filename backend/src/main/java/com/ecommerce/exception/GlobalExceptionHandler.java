package com.ecommerce.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para todos los controladores REST.
 *
 * Intercepta:
 *  1. EcommerceException y sus subclases (jerarquía propia del dominio)
 *  2. MethodArgumentNotValidException (@Valid falló en el RequestBody)
 *  3. IllegalArgumentException (argumento inválido en lógica)
 *  4. AccessDeniedException (Spring Security — 403 Framework-level)
 *  5. Exception genérica (fallback — 500 sin exponer stack trace)
 *
 * Todos retornan el mismo formato ErrorResponse:
 *  { "timestamp", "codigo", "mensaje", "path" }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 1. Excepciones de dominio propias ─────────────────────────────────────
    @ExceptionHandler(EcommerceException.class)
    public ResponseEntity<ErrorResponse> handleEcommerceException(
            EcommerceException ex, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                ex.getCodigo(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(body);
    }

    // ── 2. Validación de @RequestBody con @Valid ───────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // Concatenar todos los campos que fallaron
        String campos = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ErrorResponse body = new ErrorResponse(
                "VAL_001",
                "Error de validación: " + campos,
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    // ── 3. IllegalArgumentException ───────────────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                "VAL_002",
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    // ── 4. AccessDeniedException de Spring Security ───────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                "ACC_001",
                "Acceso denegado: no tienes permisos para esta operación.",
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(body);
    }

    // ── 5. Fallback genérico (500) ────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        // Loguear internamente sin exponer detalles al cliente
        System.err.println("[GlobalExceptionHandler] Error no manejado: " + ex.getClass().getName()
                + " — " + ex.getMessage());

        ErrorResponse body = new ErrorResponse(
                "SYS_001",
                "Error interno del servidor. Por favor contacta al administrador.",
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}

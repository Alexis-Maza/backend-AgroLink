package AgroLink.AgroLink.web.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Manejador global de excepciones para la API REST.
 *
 * Convierte excepciones comunes lanzadas en servicios a respuestas HTTP
 * estructuradas con cuerpo JSON, evitando que Spring devuelva stack traces
 * o respuestas genéricas 500.
 *
 * Excepciones cubiertas:
 *  - NoSuchElementException  → 404 Not Found
 *  - AccessDeniedException   → 403 Forbidden
 *  - IllegalArgumentException → 400 Bad Request
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Recurso no encontrado (ej. cultivo con id inexistente).
     * Lanzada en TrazabilidadService cuando el cultivo no existe en BD.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Acceso denegado a nivel de negocio.
     * Lanzada en TrazabilidadService cuando un AGRICULTOR intenta ver
     * un cultivo que no le pertenece.
     * Nota: Spring Security también puede lanzar AccessDeniedException
     * desde @PreAuthorize (roles incorrectos), que se captura aquí también.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Argumento inválido en la capa de servicio.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}

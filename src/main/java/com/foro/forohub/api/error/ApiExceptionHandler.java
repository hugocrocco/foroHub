package com.foro.forohub.api.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    // ===== 400 - Validación (Bean Validation en @RequestBody) =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        List<FieldViolation> violations = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            violations.add(new FieldViolation(fe.getField(), fe.getDefaultMessage()));
        }
        return build(HttpStatus.BAD_REQUEST, "Validación fallida", ex.getMessage(), req, violations);
    }

    // ===== 400 - Validación (ConstraintViolation en @RequestParam/@PathVariable) =====
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest req) {
        List<FieldViolation> violations = new ArrayList<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            violations.add(new FieldViolation(
                    cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : "",
                    cv.getMessage()
            ));
        }
        return build(HttpStatus.BAD_REQUEST, "Parámetros inválidos", ex.getMessage(), req, violations);
    }

    // ===== 400 - JSON mal formado / tipo incorrecto =====
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Cuerpo de la solicitud inválido", ex.getMostSpecificCause().getMessage(), req, null);
    }

    // ===== 405 - Método no soportado =====
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                  HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método no soportado", ex.getMessage(), req, null);
    }

    // ===== 400 - Falta parámetro requerido =====
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
                                                            HttpServletRequest req) {
        List<FieldViolation> violations = List.of(new FieldViolation(ex.getParameterName(), "Parámetro requerido"));
        return build(HttpStatus.BAD_REQUEST, "Parámetro faltante", ex.getMessage(), req, violations);
    }

    // ===== 404 - No encontrado =====
    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), req, null);
    }

    // ===== 409 - Conflicto de integridad (FK/UK, etc.) =====
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Conflicto de integridad de datos", ex.getMostSpecificCause().getMessage(), req, null);
    }

    // ===== 422 - Regla de negocio / duplicados =====
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBusiness(RuntimeException ex, HttpServletRequest req) {
        // Usamos 422 Unprocessable Entity para reglas de negocio/duplicados
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio violada", ex.getMessage(), req, null);
    }

    // ===== 500 - Fallback =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", ex.getMessage(), req, null);
    }

    // ========= Helpers =========
    private ResponseEntity<ErrorResponse> build(HttpStatus status,
                                                String error,
                                                String message,
                                                HttpServletRequest req,
                                                List<FieldViolation> violations) {
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                error,
                message,
                req.getRequestURI(),
                violations != null ? violations : List.of()
        );
        return ResponseEntity.status(status).body(body);
    }

    // DTOs de respuesta (inmutables)
    public record ErrorResponse(
            String timestamp,
            int status,
            String error,
            String message,
            String path,
            List<FieldViolation> violations
    ) {}

    public record FieldViolation(
            String field,
            String message
    ) {}
}
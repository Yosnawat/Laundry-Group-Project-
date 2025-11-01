package config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for all REST controllers
 * Centralizes error handling and response formatting
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle resource not found errors
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {
        return createErrorResponse(ex.getMessage(), "NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    /**
     * Handle unauthorized access
     */
    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<?> handleIllegalAccessException(
            IllegalAccessException ex,
            WebRequest request) {
        return createErrorResponse(ex.getMessage(), "UNAUTHORIZED", HttpStatus.FORBIDDEN);
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest request) {
        return createErrorResponse(ex.getMessage(), "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(
            Exception ex,
            WebRequest request) {
        return createErrorResponse("Internal server error", "SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Create standardized error response
     */
    private ResponseEntity<?> createErrorResponse(String message, String code, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("code", code);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, status);
    }
}

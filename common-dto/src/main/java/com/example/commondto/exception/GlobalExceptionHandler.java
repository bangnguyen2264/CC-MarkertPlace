package com.example.commondto.exception;

import com.example.commondto.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 🧩 Validation error (DTO @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    // 🔹 Custom domain exceptions
    @ExceptionHandler({NotFoundException.class, BadRequestException.class, UnauthorizedException.class, AccessDeniedException.class, ConflictException.class})
    public ResponseEntity<ErrorResponse> handleCustomExceptions(RuntimeException ex, WebRequest webRequest) {
        HttpStatus status;

        if (ex instanceof NotFoundException) status = HttpStatus.NOT_FOUND;
        else if (ex instanceof BadRequestException) status = HttpStatus.BAD_REQUEST;
        else if (ex instanceof UnauthorizedException) status = HttpStatus.UNAUTHORIZED;
        else if (ex instanceof AccessDeniedException) status = HttpStatus.FORBIDDEN;
        else if (ex instanceof ConflictException) status = HttpStatus.CONFLICT;
        else status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse error = new ErrorResponse(new Date(), status.value(), status.getReasonPhrase(), ex.getMessage(), webRequest.getDescription(false));

        return ResponseEntity.status(status).body(error);
    }

    // 🔹 Hibernate / SQL specific exceptions
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {

        String message = "Database constraint violation";
        if (ex.getCause() instanceof ConstraintViolationException constraintEx) {
            message = constraintEx.getConstraintName();
        }

        ErrorResponse error = new ErrorResponse(new Date(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), message, request.getRequestURI());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(SQLGrammarException.class)
    public ResponseEntity<ErrorResponse> handleSQLGrammarException(SQLGrammarException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(new Date(), HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Invalid SQL syntax.", request.getRequestURI());
        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(JDBCConnectionException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseConnection(JDBCConnectionException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(new Date(), HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(), "Unable to connect to the database.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(SQLException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(new Date(), HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "A database error occurred: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.internalServerError().body(error);
    }

    // 🔹 Fallback handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {

        ex.printStackTrace(); // for debugging

        ErrorResponse error = new ErrorResponse(new Date(), HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "An unexpected error occurred.", request.getRequestURI());

        return ResponseEntity.internalServerError().body(error);
    }
}


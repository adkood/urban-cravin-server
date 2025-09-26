package com.ashutosh.urban_cravin.configs;

import com.ashutosh.urban_cravin.helpers.dtos.ApiResponse;
import com.ashutosh.urban_cravin.helpers.enums.Status;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation errors (DTO field validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        System.out.println(e);
        String errorMsg = e.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(new ApiResponse(Status.Fail, errorMsg, null));
    }

    // Handle invalid type (e.g., String instead of UUID/Number in path variable)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        System.out.println(e);
        String errorMsg = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                e.getValue(), e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");
        return ResponseEntity.badRequest()
                .body(new ApiResponse(Status.Fail, errorMsg, null));
    }

    // Handle missing required query params
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingParams(MissingServletRequestParameterException e) {
        System.out.println(e);
        String errorMsg = "Missing required parameter: " + e.getParameterName();
        return ResponseEntity.badRequest()
                .body(new ApiResponse(Status.Fail, errorMsg, null));
    }

    // Handle resource not found
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse> handleNotFound(NoSuchElementException e) {
        System.out.println(e);
        return ResponseEntity.status(404)
                .body(new ApiResponse(Status.Fail, e.getMessage(), null));
    }

    // Handle DB unique constraint violation (e.g., duplicate SKU)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        System.out.println(e);
        return ResponseEntity.badRequest()
                .body(new ApiResponse(Status.Fail, "Database constraint violation: " + e.getMostSpecificCause().getMessage(), null));
    }

    // Handle custom business/runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntimeExceptions(RuntimeException e) {
        System.out.println(e);
        return ResponseEntity.badRequest()
                .body(new ApiResponse(Status.Fail, e.getMessage(), null));
    }

    // Fallback - unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception e) {
        System.out.println(e);
        return ResponseEntity.internalServerError()
                .body(new ApiResponse(Status.Error, "Something went wrong", null));
    }
}

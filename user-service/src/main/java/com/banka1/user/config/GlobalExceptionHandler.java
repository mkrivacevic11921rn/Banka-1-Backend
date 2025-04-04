package com.banka1.user.config;

import com.banka1.user.utils.ResponseTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        return ResponseTemplate.create(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR),
                false,
                Map.of(
                        "stacktrace", ex.getStackTrace()
                ),
                ex.getMessage()
        );
    }
}

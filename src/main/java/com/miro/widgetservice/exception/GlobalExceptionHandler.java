package com.miro.widgetservice.exception;

import com.miro.widgetservice.dto.ValidationDto;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WidgetServiceException.class)
    public String handleBadRequestException(WidgetServiceException exception) {
        String message = exception.getMessage();
        log.error(message, exception);

        return message;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ValidationDto handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        log.debug("Object validation failed", exception);

        Map<String, String> validationErrorsMap = exception.getBindingResult()
            .getAllErrors()
            .stream()
            .collect(
                Collectors.toMap(
                    error -> error instanceof FieldError ? ((FieldError)error).getField() : error.getObjectName(),
                    error -> String.valueOf(error.getDefaultMessage()),
                    (left, right) -> left
                )
            );

        return ValidationDto.builder()
            .success(false)
            .errorMessage("Invalid request")
            .payload(validationErrorsMap)
            .timestamp(LocalDateTime.now())
            .build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Exception exception) {
        String message = exception.getMessage();
        log.error(message, exception);

        return message;
    }
}

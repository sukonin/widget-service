package com.miro.widgetservice.exception;

import com.miro.widgetservice.dto.ValidationDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
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
    public List<ValidationDto> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        BindingResult result = exception.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        List<ValidationDto> validationDtos = fieldErrors.stream()
            .map(this::processFieldError)
            .collect(Collectors.toList());

        log.error(validationDtos.toString(), exception);

        return validationDtos;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Exception exception) {
        String message = exception.getMessage();
        log.error(message, exception);

        return message;
    }

    private ValidationDto processFieldError(FieldError error) {
        return ValidationDto.builder()
            .field(error.getField())
            .message(error.getDefaultMessage())
            .build();
    }
}

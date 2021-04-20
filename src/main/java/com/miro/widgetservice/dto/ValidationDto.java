package com.miro.widgetservice.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ValidationDto {

    boolean success;

    Object payload;

    String errorMessage;

    LocalDateTime timestamp;

}

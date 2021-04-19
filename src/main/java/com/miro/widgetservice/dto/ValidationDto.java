package com.miro.widgetservice.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ValidationDto {

    String field;

    String message;
}

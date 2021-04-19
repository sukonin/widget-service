package com.miro.widgetservice.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SearchAreaDto {

    @NotNull
    Integer xPoint1;

    @NotNull
    Integer yPoint1;

    @NotNull
    Integer xPoint2;

    @NotNull
    Integer yPoint2;

}

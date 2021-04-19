package com.miro.widgetservice.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class WidgetReqDto {

    private Long id;

    @NotNull
    private Integer xPoint;

    @NotNull
    private Integer yPoint;

    private Integer zIndex;

    @NotNull
    @Positive
    private Integer width;

    @NotNull
    @Positive
    private Integer height;
}

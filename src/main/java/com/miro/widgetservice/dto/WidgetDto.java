package com.miro.widgetservice.dto;

import java.time.LocalDateTime;
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
public class WidgetDto {

    private Long id;

    @NotNull
    private Float xPoint;

    @NotNull
    private Float yPoint;

    private Integer zIndex;

    @NotNull
    @Positive
    private Integer width;

    @NotNull
    @Positive
    private Integer height;

    private LocalDateTime modificationDate;
}

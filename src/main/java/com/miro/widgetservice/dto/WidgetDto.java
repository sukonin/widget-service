package com.miro.widgetservice.dto;

import com.miro.widgetservice.repository.IdGenerator;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class WidgetDto {

    private Long id = new IdGenerator().generateNextId();

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

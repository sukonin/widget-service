package com.miro.widgetservice.dto;

import java.time.LocalDateTime;
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
public class WidgetRespDto {
    private Long id;

    private Integer xPoint;

    private Integer yPoint;

    private Integer zIndex;

    private Integer width;

    private Integer height;

    private LocalDateTime modificationDate;
}

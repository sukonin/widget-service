package com.miro.widgetservice.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class WidgetInMemory {

    private Long id;

    private Float xPoint;

    private Float yPoint;

    private Integer zIndex;

    private Integer width;

    private Integer height;

    private LocalDateTime modificationDate;
}


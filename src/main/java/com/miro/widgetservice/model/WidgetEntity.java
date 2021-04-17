package com.miro.widgetservice.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class WidgetEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private Float xPoint;

    @Column(nullable = false)
    private Float yPoint;

    @Column(nullable = false)
    private Integer zIndex;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(nullable = false)
    private LocalDateTime modificationDate;
}

package com.miro.widgetservice.model;

import java.time.LocalDateTime;
import java.util.Objects;
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
public class Widget {

    private Long id;

    private Float xPoint;

    private Float yPoint;

    private Integer zIndex;

    private Integer width;

    private Integer height;

    private LocalDateTime modificationDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Widget)) {
            return false;
        }
        Widget widget = (Widget)o;
        return getId().equals(widget.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}


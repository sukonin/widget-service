package com.miro.widgetservice.converter;

import com.miro.widgetservice.dto.WidgetDto;
import com.miro.widgetservice.model.WidgetInMemory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WidgetConverter {

    public WidgetDto convert(WidgetInMemory widget) {
        return WidgetDto.builder()
                .id(widget.getId())
                .xPoint(widget.getXPoint())
                .yPoint(widget.getYPoint())
                .zIndex(widget.getZIndex())
                .width(widget.getWidth())
                .height(widget.getHeight())
                .modificationDate(widget.getModificationDate())
                .build();
    }

    public WidgetInMemory convert(WidgetDto widgetDto) {
        return WidgetInMemory.builder()
                .id(widgetDto.getId())
                .xPoint(widgetDto.getXPoint())
                .yPoint(widgetDto.getYPoint())
                .zIndex(widgetDto.getZIndex())
                .width(widgetDto.getWidth())
                .height(widgetDto.getHeight())
                .modificationDate(LocalDateTime.now())
                .build();
    }
}

package com.miro.widgetservice.converter;

import com.miro.widgetservice.dto.WidgetDto;
import com.miro.widgetservice.model.Widget;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WidgetConverter {

    private final ModelMapper modelMapper;

    public WidgetDto convert(Widget widget) {
        return modelMapper.map(widget, WidgetDto.class);
    }

    public Widget convert(WidgetDto widgetDto) {
        return modelMapper.map(widgetDto, Widget.class);
    }
}

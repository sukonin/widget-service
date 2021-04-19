package com.miro.widgetservice.converter;

import com.miro.widgetservice.dto.WidgetReqDto;
import com.miro.widgetservice.dto.WidgetRespDto;
import com.miro.widgetservice.model.Widget;
import com.miro.widgetservice.model.WidgetEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WidgetConverter {

    private final ModelMapper modelMapper;

    public WidgetRespDto convert(Widget widget) {
        return modelMapper.map(widget, WidgetRespDto.class);
    }

    public Widget convert(WidgetReqDto widgetReqDto) {
        return modelMapper.map(widgetReqDto, Widget.class);
    }

    public WidgetEntity convertEntity(WidgetReqDto widgetReqDto) {
        return modelMapper.map(widgetReqDto, WidgetEntity.class);
    }

    public WidgetRespDto convertEntity(WidgetEntity widgetEntity) {
        return modelMapper.map(widgetEntity, WidgetRespDto.class);
    }
}

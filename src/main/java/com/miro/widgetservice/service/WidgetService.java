package com.miro.widgetservice.service;

import com.miro.widgetservice.dto.WidgetDto;

import java.util.List;

public interface WidgetService {

    WidgetDto create(WidgetDto widgetDto);

    WidgetDto update(Long id, WidgetDto widgetDto);

    WidgetDto findById(Long id);

    List<WidgetDto> findAll();

    void deleteById(Long id);

}

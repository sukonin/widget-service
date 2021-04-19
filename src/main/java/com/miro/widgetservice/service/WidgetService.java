package com.miro.widgetservice.service;

import com.miro.widgetservice.dto.SearchAreaDto;
import com.miro.widgetservice.dto.WidgetReqDto;
import com.miro.widgetservice.dto.WidgetRespDto;
import java.util.List;

public interface WidgetService {

    WidgetRespDto create(WidgetReqDto widgetReqDto);

    WidgetRespDto update(Long id, WidgetReqDto widgetReqDto);

    WidgetRespDto findById(Long id);

    List<WidgetRespDto> findAll(Integer page, Integer size, SearchAreaDto searchAreaDto);

    void deleteById(Long id);

}

package com.miro.widgetservice.service.impl;

import com.miro.widgetservice.converter.WidgetConverter;
import com.miro.widgetservice.dto.SearchAreaDto;
import com.miro.widgetservice.dto.WidgetReqDto;
import com.miro.widgetservice.dto.WidgetRespDto;
import com.miro.widgetservice.exception.WidgetServiceException;
import com.miro.widgetservice.model.Widget;
import com.miro.widgetservice.repository.WidgetRepository;
import com.miro.widgetservice.service.WidgetService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("memory")
@Slf4j
@Service
@RequiredArgsConstructor
public class WidgetServiceImpl implements WidgetService {

    private final WidgetRepository widgetRepository;

    private final WidgetConverter widgetConverter;

    @Override
    public WidgetRespDto create(WidgetReqDto widgetReqDto) {
        log.info("Create new widget {}", widgetReqDto);
        Widget widgetToUpdate = widgetConverter.convert(widgetReqDto);
        widgetToUpdate.setModificationDate(LocalDateTime.now());
        Widget savedWidget = widgetRepository.save(widgetToUpdate);

        return widgetConverter.convert(savedWidget);
    }

    @Override
    public WidgetRespDto update(Long id, WidgetReqDto widgetReqDto) {
        log.info("Update widget {}", widgetReqDto);
        if (isExist(id)) {
            Widget widget = widgetConverter.convert(widgetReqDto);
            widget.setId(id);
            widget.setModificationDate(LocalDateTime.now());
            Widget savedWidget = widgetRepository.save(widget);
            return widgetConverter.convert(savedWidget);
        }

        throw new WidgetServiceException(getErrorMessage(id));
    }

    @Override
    public WidgetRespDto findById(Long id) {
        log.info("Find widget by id {}", id);

        return widgetRepository.findById(id)
            .map(widgetConverter::convert)
            .orElseThrow(() -> new WidgetServiceException(getErrorMessage(id)));
    }

    @Override
    public List<WidgetRespDto> findAll(Integer page, Integer size, SearchAreaDto searchAreaDto) {
        log.info("Find all widgets. Page: {} and Size: {}", page, size);

        if (Objects.isNull(page)) {
            return widgetRepository.findAll(searchAreaDto).stream()
                .map(widgetConverter::convert)
                .collect(Collectors.toList());
        }
        if (page < 0) {
            throw new WidgetServiceException("Invalid page argument");
        }
        if (!Objects.isNull(size) && (size < 0 || size > 500)) {
            throw new WidgetServiceException("Invalid size argument");
        }

        return widgetRepository.findAll(page, size, searchAreaDto).stream()
            .map(widgetConverter::convert)
            .collect(Collectors.toList());
    }

    @Override
    public List<WidgetRespDto> findAll() {
        return widgetRepository.findAll().stream()
            .map(widgetConverter::convert)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        log.info("Delete widget by id {}", id);

        if (isExist(id)) {
            widgetRepository.deleteById(id);
            return;
        }

        throw new WidgetServiceException(getErrorMessage(id));
    }

    @Override
    public void deleteAll() {
        widgetRepository.deleteAll();
    }

    @Override
    public List<WidgetRespDto> saveAll(List<WidgetReqDto> widgetList) {
        return widgetList.stream()
            .map(this::create)
            .collect(Collectors.toList());
    }

    private boolean isExist(Long id) {
        log.info("Widget is exist by id {}", id);
        return widgetRepository.isExist(id);
    }

    private String getErrorMessage(Long id) {
        return "Widget with id " + id + " does not exist";
    }
}
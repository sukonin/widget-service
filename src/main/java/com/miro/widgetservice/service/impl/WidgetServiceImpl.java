package com.miro.widgetservice.service.impl;

import com.miro.widgetservice.converter.WidgetConverter;
import com.miro.widgetservice.dto.WidgetDto;
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
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WidgetServiceImpl implements WidgetService {

    private final WidgetRepository widgetRepository;

    private final WidgetConverter widgetConverter;

    @Override
    public WidgetDto create(WidgetDto widgetDto) {
        log.info("Create new widget {}", widgetDto);
        Widget widgetToUpdate = widgetConverter.convert(widgetDto);
        widgetToUpdate.setModificationDate(LocalDateTime.now());
        Widget savedWidget = widgetRepository.save(widgetToUpdate);

        return widgetConverter.convert(savedWidget);
    }

    @Override
    public WidgetDto update(Long id, WidgetDto widgetDto) {
        widgetDto.setId(id);
        log.info("Update widget {}", widgetDto);
        if (isExist(id)) {
            Widget widget = widgetConverter.convert(widgetDto);
            widget.setModificationDate(LocalDateTime.now());
            Widget savedWidget = widgetRepository.save(widget);
            return widgetConverter.convert(savedWidget);
        }

        throw new WidgetServiceException("Widget with id " + id + " does not exist");
    }

    @Override
    public WidgetDto findById(Long id) {
        log.info("Find widget by id {}", id);

        Widget existWidget = widgetRepository.findById(id)
            .orElseThrow(() -> new WidgetServiceException("Widget with id " + id + " does not exist"));

        return widgetConverter.convert(existWidget);
    }

    @Override
    public List<WidgetDto> findAll(Integer page) {
        log.info("Find all widgets. Page: {}", page);

        if (Objects.isNull(page)) {
            return widgetRepository.findAll().stream()
                .map(widgetConverter::convert)
                .collect(Collectors.toList());
        }
        if (page < 0) {
            throw new WidgetServiceException("Page cannot be negative");
        }
        return widgetRepository.findAll(page).stream()
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
        throw new WidgetServiceException("Widget with id " + id + " does not exist");
    }

    private boolean isExist(Long id) {
        log.info("Widget is exist by id {}", id);

        return widgetRepository.isExist(id);
    }
}
package com.miro.widgetservice.service.impl;

import com.miro.widgetservice.converter.WidgetConverter;
import com.miro.widgetservice.dto.WidgetDto;
import com.miro.widgetservice.exception.WidgetServiceException;
import com.miro.widgetservice.model.WidgetInMemory;
import com.miro.widgetservice.repository.IdGenerator;
import com.miro.widgetservice.repository.WidgetRepository;
import com.miro.widgetservice.service.WidgetService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.miro.widgetservice.repository.WidgetMemoryRepository.getWidgetDatasource;

@Service
@RequiredArgsConstructor
public class WidgetServiceImpl implements WidgetService {

    private final WidgetRepository widgetRepository;

    private final WidgetConverter widgetConverter;

    private final IdGenerator idGenerator;

    @Override
    public WidgetDto create(WidgetDto widgetDto) {

        WidgetInMemory widget = widgetConverter.convert(widgetDto);

        WidgetInMemory byZIndex = widgetRepository.findByZIndex(widget.getZIndex());
        if (byZIndex == null) {
            long id = idGenerator.generateNextId();
            widget.setId(id);
            getWidgetDatasource().put(id, widget);
        } else {

            WidgetInMemory existByZ = getWidgetDatasource().values()
                .stream()
                .filter(w -> w.getZIndex().equals(widget.getZIndex()))
                .findFirst()
                .orElseThrow();

            getWidgetDatasource().values().stream().reduce(existByZ, (l, r) -> {

                return l;
            });

        }
        return null;
    }

    @Override
    public WidgetDto update(Long id, WidgetDto widgetDto) {
        widgetDto.setId(id);

        if (isExist(id)) {
            WidgetInMemory widget = widgetConverter.convert(widgetDto);
            widgetRepository.save(widget);
            return widgetDto;
        }

        throw new WidgetServiceException("Widget with id " + id + " does not exist");
    }

    @Override
    public WidgetDto findById(Long id) {
        WidgetInMemory savedWidget = widgetRepository.findById(id).orElseThrow(() -> new WidgetServiceException("Widget does not exist"));
        return widgetConverter.convert(savedWidget);
    }

    @Override
    public List<WidgetDto> findAll() {
        return widgetRepository.findAll().stream()
            .map(widgetConverter::convert)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        widgetRepository.deleteById(id);
    }

    private boolean isExist(Long id) {
        return widgetRepository.isExist(id);
    }
}
package com.miro.widgetservice.repository;

import com.miro.widgetservice.model.WidgetInMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class WidgetMemoryRepository implements WidgetRepository {

    private static final Map<Long, WidgetInMemory> widgetDatasource = new LinkedHashMap<>();

    private final IdGenerator idGenerator;

    public static Map<Long, WidgetInMemory> getWidgetDatasource() {
        return widgetDatasource;
    }

    @Override
    public Optional<WidgetInMemory> findById(Long id) {
        log.info("Find widget by id {}", id);
        return Optional.of(widgetDatasource.get(id));
    }

    @Override
    public List<WidgetInMemory> findAll() {
        log.info("Find all widgets");
        return List.copyOf(widgetDatasource.values());
    }

    @Override
    public WidgetInMemory save(WidgetInMemory widget) {
        if (Objects.isNull(widget.getId())) {
            return persist(widget);
        }
        return merge(widget);
    }

    @Override
    public void deleteById(Long id) {
        widgetDatasource.remove(id);
    }

    @Override
    public boolean isExist(Long id) {
        return widgetDatasource.containsKey(id);
    }

    @Override
    public WidgetInMemory findByZIndex(Integer zIndex) {
        return widgetDatasource.values()
                .stream()
                .filter(w -> w.getZIndex().equals(zIndex))
                .findFirst()
                .orElseThrow();
    }

    private WidgetInMemory persist(WidgetInMemory widget) {
        widget.setId(idGenerator.generateNextId());
        widgetDatasource.put(widget.getId(), widget);
        log.info("Persist new widget {}", widget);
        return widget;
    }

    private WidgetInMemory merge(WidgetInMemory widget) {
        log.info("Update widget {}", widget);
        return widgetDatasource.put(widget.getId(), widget);
    }
}

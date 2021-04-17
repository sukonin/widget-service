package com.miro.widgetservice.repository;

import com.miro.widgetservice.exception.WidgetServiceException;
import com.miro.widgetservice.model.Widget;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WidgetInMemoryRepository implements WidgetRepository {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private static final Map<Long, Widget> mainStorage = new HashMap<>();

    private static final NavigableMap<Integer, Widget> zIndexMap = new ConcurrentSkipListMap<>();

    private static final AtomicLong atomicIdGenerator = new AtomicLong();

    @Override
    public Widget save(Widget widget) {
        if (Objects.isNull(widget.getId())) {
            return persist(widget);
        }
        return merge(widget);
    }

    @Override
    public Optional<Widget> findById(Long id) {
        return Optional.ofNullable(mainStorage.get(id));
    }

    @Override
    public List<Widget> findAll() {
        return List.copyOf(zIndexMap.values());
    }

    @Override
    public List<Widget> findAll(Integer page) {
        List<Widget> widgets = List.copyOf(zIndexMap.values());

        int fromIndex = page * DEFAULT_PAGE_SIZE;

        if (widgets.isEmpty() || widgets.size() < fromIndex) {
            return Collections.emptyList();
        }

        return widgets.subList(fromIndex, Math.min(fromIndex + DEFAULT_PAGE_SIZE, widgets.size()));
    }

    @Override
    public void deleteById(Long id) {
        Widget widgetToRemove = mainStorage.remove(id);
        zIndexMap.remove(widgetToRemove.getZIndex());
    }

    @Override
    public boolean isExist(Long id) {
        return mainStorage.containsKey(id);
    }

    @Override
    public void deleteAll() {
        mainStorage.clear();
        zIndexMap.clear();
    }

    @Override
    public List<Widget> saveAll(List<Widget> widgetList) {
        return widgetList.stream()
            .map(this::save)
            .collect(Collectors.toList());
    }

    private Widget persist(Widget widget) {
        Integer zIndex = widget.getZIndex();

        if (Objects.isNull(zIndex)) {
            zIndex = getLastIndex();
        } else {
            if (zIndexMap.containsKey(zIndex)) {
                shiftAndIncrement(zIndex);
            }
        }

        widget.setId(atomicIdGenerator.incrementAndGet());
        widget.setZIndex(zIndex);

        return saveOrUpdate(widget);
    }

    private Widget merge(Widget widgetForUpdate) {
        Widget savedWidget = mainStorage.get(widgetForUpdate.getId());

        Integer zIndex = widgetForUpdate.getZIndex();

        if (Objects.isNull(zIndex)) {
            if (isLastWidgetTheSameAsUpdate(widgetForUpdate)) {
                zIndex = savedWidget.getZIndex();
            } else {
                zIndex = getLastIndex();
            }
            widgetForUpdate.setZIndex(zIndex);
        }

        return update(widgetForUpdate, savedWidget);
    }

    private Widget update(Widget widgetForUpdate, Widget savedWidget) {
        if (isNewPosition(widgetForUpdate, savedWidget)) {
            zIndexMap.remove(savedWidget.getZIndex());
            if (zIndexMap.containsKey(widgetForUpdate.getZIndex())) {
                shiftAndIncrement(widgetForUpdate.getZIndex());
            }
        }
        return saveOrUpdate(widgetForUpdate);
    }

    private Widget saveOrUpdate(Widget widget) {
        mainStorage.put(widget.getId(), widget);
        zIndexMap.put(widget.getZIndex(), widget);
        return widget;
    }

    private boolean isNewPosition(Widget widgetForUpdate, Widget savedWidget) {
        return !widgetForUpdate.getZIndex().equals(savedWidget.getZIndex()) && widgetForUpdate.getId().equals(savedWidget.getId());
    }

    private boolean isLastWidgetTheSameAsUpdate(Widget widgetForUpdate) {
        return zIndexMap.lastEntry().getValue().getId().equals(widgetForUpdate.getId());
    }

    private int getLastIndex() {
        if (zIndexMap.isEmpty()) {
            return 0;
        }
        if (zIndexMap.lastKey().equals(Integer.MAX_VALUE)) {
            throw new WidgetServiceException("Z Index reach maximum");
        }
        return zIndexMap.lastKey() + 1;
    }

    private void shiftAndIncrement(Integer zIndex) {
        SortedMap<Integer, Widget> tailMap = zIndexMap.tailMap(zIndex);

        if (tailMap.isEmpty()) {
            return;
        }

        Integer lastZIndex = getLastZIndexWithoutGap(tailMap.keySet());

        SortedMap<Integer, Widget> integerWidgetSortedMap = tailMap.subMap(zIndex, lastZIndex + 1);
        integerWidgetSortedMap.values().forEach(widget -> {
            Integer newZIndex = widget.getZIndex() + 1;
            widget.setZIndex(newZIndex);
            widget.setModificationDate(LocalDateTime.now());
            zIndexMap.put(newZIndex, widget);
        });
    }

    private Integer getLastZIndexWithoutGap(Set<Integer> integers) {
        Integer lastZIndex = null;
        for (Integer currentIndex : integers) {
            if (lastZIndex == null) {
                lastZIndex = currentIndex;
            } else if (currentIndex == lastZIndex + 1) {
                lastZIndex = currentIndex;
            } else {
                break;
            }
        }
        if (Integer.MAX_VALUE == Objects.requireNonNull(lastZIndex) && integers.contains(lastZIndex)) {
            throw new WidgetServiceException("Z Index reach maximum");
        }
        return lastZIndex;
    }
}

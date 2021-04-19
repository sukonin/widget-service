package com.miro.widgetservice.repository.memory;

import com.miro.widgetservice.dto.SearchAreaDto;
import com.miro.widgetservice.exception.WidgetServiceException;
import com.miro.widgetservice.model.Widget;
import com.miro.widgetservice.repository.WidgetRepository;
import com.miro.widgetservice.struct.RTreeStorage;
import com.miro.widgetservice.util.WidgetUtil;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
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

    public static final int DEFAULT_PAGE_SIZE = 10;

    private final AtomicLong atomicIdGenerator = new AtomicLong();

    private final RTreeStorage coordinateStorage;

    private final WidgetUtil widgetUtil;

    private Map<Long, Widget> widgetStorage = new ConcurrentHashMap<>();

    private NavigableMap<Integer, Widget> zIndexStorage = new ConcurrentSkipListMap<>();

    @Override
    public Widget save(Widget widget) {
        if (Objects.isNull(widget.getId())) {
            return persist(widget);
        }
        return merge(widget);
    }

    @Override
    public Optional<Widget> findById(Long id) {
        return Optional.ofNullable(widgetStorage.get(id));
    }

    @Override
    public List<Widget> findAll() {
        return List.copyOf(zIndexStorage.values());
    }

    @Override
    public List<Widget> findAll(Integer page, Integer size, SearchAreaDto searchAreaDto) {
        List<Widget> widgets = findAll(searchAreaDto);

        int resultsPerPage = size == null ? DEFAULT_PAGE_SIZE : size;
        int fromIndex = page * resultsPerPage;

        if (widgets.isEmpty() || widgets.size() < fromIndex) {
            log.info("Store is empty");
            return Collections.emptyList();
        }

        return widgets.subList(fromIndex, Math.min(fromIndex + resultsPerPage, widgets.size()));
    }

    @Override
    public List<Widget> findAll(SearchAreaDto searchAreaDto) {
        if (!isSearchDtoValid(searchAreaDto)) {
            return findAll();
        }

        return coordinateStorage.findInArea(searchAreaDto);
    }

    @Override
    public void deleteById(Long id) {
        Widget widgetToRemove = widgetStorage.remove(id);
        zIndexStorage.remove(widgetToRemove.getZIndex());
        coordinateStorage.deleteIfExist(widgetToRemove);
    }

    @Override
    public boolean isExist(Long id) {
        return widgetStorage.containsKey(id);
    }

    @Override
    public void deleteAll() {
        widgetStorage = new ConcurrentHashMap<>();
        zIndexStorage = new ConcurrentSkipListMap<>();
        coordinateStorage.deleteAll();
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
            log.info("Z index is null. Generate new: {}", zIndex);
        } else if (zIndexStorage.containsKey(zIndex)) {
            log.info("Z index already exist. Shift and increment existed {}", zIndex);
            shiftAndIncrement(zIndex);
        }

        widget.setId(atomicIdGenerator.incrementAndGet());
        widget.setZIndex(zIndex);

        return saveOrUpdate(widget);
    }

    private Widget merge(Widget widgetForUpdate) {
        Widget savedWidget = widgetStorage.get(widgetForUpdate.getId());

        Integer zIndex = widgetForUpdate.getZIndex();

        if (Objects.isNull(zIndex)) {
            if (isLastWidgetTheSameAsUpdate(widgetForUpdate)) {
                zIndex = savedWidget.getZIndex();
            } else {
                zIndex = getLastIndex();
            }
            widgetForUpdate.setZIndex(zIndex);
        }

        return merge(widgetForUpdate, savedWidget);
    }

    private Widget merge(Widget widgetForUpdate, Widget savedWidget) {
        if (isNewPosition(widgetForUpdate, savedWidget)) {
            zIndexStorage.remove(savedWidget.getZIndex());
            if (zIndexStorage.containsKey(widgetForUpdate.getZIndex())) {
                shiftAndIncrement(widgetForUpdate.getZIndex());
            }
        }
        return saveOrUpdate(widgetForUpdate);
    }

    private Widget saveOrUpdate(Widget widget) {
        widgetStorage.put(widget.getId(), widget);
        zIndexStorage.put(widget.getZIndex(), widget);
        coordinateStorage.putOrReplace(widget);
        return widget;
    }

    private boolean isNewPosition(Widget widgetForUpdate, Widget savedWidget) {
        return !widgetForUpdate.getZIndex().equals(savedWidget.getZIndex()) && widgetForUpdate.getId().equals(savedWidget.getId());
    }

    private boolean isLastWidgetTheSameAsUpdate(Widget widgetForUpdate) {
        return zIndexStorage.lastEntry().getValue().getId().equals(widgetForUpdate.getId());
    }

    private int getLastIndex() {
        if (zIndexStorage.isEmpty()) {
            return 0;
        }
        if (zIndexStorage.lastKey().equals(Integer.MAX_VALUE)) {
            throw new WidgetServiceException("Z Index reach maximum");
        }
        return zIndexStorage.lastKey() + 1;
    }

    private void shiftAndIncrement(Integer zIndex) {
        SortedMap<Integer, Widget> tailMap = zIndexStorage.tailMap(zIndex);

        if (tailMap.isEmpty()) {
            log.info("Nothing to shift");
            return;
        }

        Integer lastZIndex = widgetUtil.getLastZIndexWithoutGap(tailMap.keySet());
        log.info("Last Z index in sequence {}", lastZIndex);

        SortedMap<Integer, Widget> integerWidgetSortedMap = tailMap.subMap(zIndex, lastZIndex + 1);

        integerWidgetSortedMap.values().forEach(w -> {
            Integer newZIndex = w.getZIndex() + 1;
            Widget widget = buildFromWidget(w, newZIndex);
            saveOrUpdate(widget);
        });
    }

    private Widget buildFromWidget(Widget widget, Integer newZIndex) {
        return Widget.builder()
            .id(widget.getId())
            .height(widget.getHeight())
            .width(widget.getWidth())
            .zIndex(newZIndex)
            .xPoint(widget.getXPoint())
            .yPoint(widget.getYPoint())
            .modificationDate(LocalDateTime.now())
            .build();
    }

    private boolean isSearchDtoValid(SearchAreaDto searchAreaDto) {
        return searchAreaDto.getXPoint1() != null &&
            searchAreaDto.getYPoint1() != null &&
            searchAreaDto.getXPoint2() != null &&
            searchAreaDto.getYPoint2() != null;
    }
}

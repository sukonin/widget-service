package com.miro.widgetservice.service.impl;

import com.miro.widgetservice.converter.WidgetConverter;
import com.miro.widgetservice.dto.SearchAreaDto;
import com.miro.widgetservice.dto.WidgetReqDto;
import com.miro.widgetservice.dto.WidgetRespDto;
import com.miro.widgetservice.exception.WidgetServiceException;
import com.miro.widgetservice.model.WidgetEntity;
import com.miro.widgetservice.repository.jpa.WidgetJpaRepository;
import com.miro.widgetservice.service.WidgetService;
import com.miro.widgetservice.util.WidgetUtil;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.miro.widgetservice.repository.memory.WidgetInMemoryRepository.DEFAULT_PAGE_SIZE;

@Slf4j
@Profile("database")
@Service
@RequiredArgsConstructor
public class WidgetServiceJpaImpl implements WidgetService {

    private final WidgetJpaRepository widgetRepository;

    private final WidgetConverter widgetConverter;

    private final WidgetUtil widgetUtil;

    @Override
    @Transactional
    public WidgetRespDto create(WidgetReqDto widgetReqDto) {
        WidgetEntity widgetEntity = widgetConverter.convertEntity(widgetReqDto);

        Integer zIndex = widgetEntity.getZIndex();
        if (Objects.isNull(zIndex)) {
            zIndex = getLastZIndex();
            log.info("Z index is null. Generate new: {}", zIndex);
        } else if (widgetRepository.existsByzIndex(zIndex)) {
            log.info("Z index already exist. Shift and increment existed {}", zIndex);
            shiftAndIncrement(zIndex);
        }

        widgetEntity.setZIndex(zIndex);
        WidgetEntity savedEntity = widgetRepository.save(widgetEntity);

        return widgetConverter.convertEntity(savedEntity);
    }

    @Override
    @Transactional
    public WidgetRespDto update(Long id, WidgetReqDto widgetReqDto) {
        widgetReqDto.setId(id);
        log.info("Update widget {}", widgetReqDto);

        WidgetEntity storedWidget = widgetRepository.findById(id)
            .orElseThrow(() -> new WidgetServiceException(getErrorMessage(id)));

        if (!Objects.isNull(storedWidget)) {
            WidgetEntity widgetForUpdate = widgetConverter.convertEntity(widgetReqDto);
            WidgetEntity updatedWidget = merge(widgetForUpdate, storedWidget);
            return widgetConverter.convertEntity(updatedWidget);
        }

        throw new WidgetServiceException(getErrorMessage(id));
    }

    @Override
    @Transactional(readOnly = true)
    public WidgetRespDto findById(Long id) {
        log.info("Find widget by id {}", id);

        return widgetRepository.findById(id)
            .map(widgetConverter::convertEntity)
            .orElseThrow(() -> new WidgetServiceException(getErrorMessage(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WidgetRespDto> findAll(Integer page, Integer size, SearchAreaDto searchAreaDto) {
        Sort sort = Sort.by(Sort.Direction.ASC, "zIndex");

        if (Objects.isNull(page)) {
            if (widgetUtil.isSearchDtoValid(searchAreaDto)) {
                return widgetRepository.findAll(searchAreaDto)
                    .stream()
                    .map(widgetConverter::convertEntity)
                    .collect(Collectors.toList());
            }
            return widgetRepository.findAll(sort)
                .stream()
                .map(widgetConverter::convertEntity)
                .collect(Collectors.toList());
        }

        if (page < 0) {
            throw new WidgetServiceException("Invalid page argument");
        }
        if (!Objects.isNull(size) && (size < 0 || size > 500)) {
            throw new WidgetServiceException("Invalid size argument");
        }
        if (Objects.isNull(size)) {
            size = DEFAULT_PAGE_SIZE;
        }

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        if (widgetUtil.isSearchDtoValid(searchAreaDto)) {
            return widgetRepository.findAll(searchAreaDto, pageRequest)
                .stream()
                .map(widgetConverter::convertEntity)
                .collect(Collectors.toList());
        }
        return widgetRepository.findAll(pageRequest)
            .stream()
            .map(widgetConverter::convertEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Delete widget by id {}", id);

        if (isExist(id)) {
            widgetRepository.deleteById(id);
            return;
        }

        throw new WidgetServiceException(getErrorMessage(id));
    }

    private WidgetEntity merge(WidgetEntity widgetForUpdate, WidgetEntity storedWidget) {
        Integer zIndex = widgetForUpdate.getZIndex();

        if (Objects.isNull(zIndex)) {
            WidgetEntity lastWidgetByZIndex = widgetRepository.findWidgetWithMaxZIndex();
            if (lastWidgetByZIndex.getId().equals(widgetForUpdate.getId())) {
                zIndex = storedWidget.getZIndex();
            } else {
                zIndex = getLastZIndex();
            }
            widgetForUpdate.setZIndex(zIndex);
        }

        if (shouldShift(widgetForUpdate, storedWidget)) {
            shiftAndIncrement(zIndex);
        }
        return widgetRepository.save(widgetForUpdate);
    }

    private boolean shouldShift(WidgetEntity widgetForUpdate, WidgetEntity storedWidget) {
        return !storedWidget.getZIndex().equals(widgetForUpdate.getZIndex())
            && widgetRepository.existsByzIndex(widgetForUpdate.getZIndex());
    }

    private Integer getLastZIndex() {
        Integer maxZIndex = widgetRepository.findMaxZIndex();
        if (maxZIndex == null) {
            return 0;
        }
        if (Integer.MAX_VALUE == maxZIndex) {
            throw new WidgetServiceException("z Index reach maximum");
        } else {
            return maxZIndex + 1;
        }
    }

    private String getErrorMessage(Long id) {
        return "Widget with id " + id + " does not exist";
    }

    private boolean isExist(Long id) {
        log.info("Widget is exist by id {}", id);
        return widgetRepository.existsById(id);
    }

    private void shiftAndIncrement(Integer zIndex) {
        List<WidgetEntity> widgetEntities = widgetRepository.findAllWithEqualOrGreaterZIndex(zIndex);

        List<Integer> zIndexSet = widgetEntities.stream()
            .map(WidgetEntity::getZIndex)
            .collect(Collectors.toList());

        Integer lastZIndexWithoutGap = widgetUtil.getLastZIndexWithoutGap(zIndexSet);
        log.info("Last Z index in sequence {}", lastZIndexWithoutGap);

        Integer shifted = widgetRepository.incrementFromIndexToIndex(zIndex, lastZIndexWithoutGap);
        log.info("{} widgets was shifted", shifted);
    }
}

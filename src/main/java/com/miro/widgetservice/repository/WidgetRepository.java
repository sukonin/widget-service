package com.miro.widgetservice.repository;


import com.miro.widgetservice.model.WidgetInMemory;

import java.util.List;
import java.util.Optional;

public interface WidgetRepository {

    Optional<WidgetInMemory> findById(Long id);

    List<WidgetInMemory> findAll();

    WidgetInMemory save(WidgetInMemory widget);

    void deleteById(Long id);

    boolean isExist(Long id);

    WidgetInMemory findByZIndex(Integer zIndex);
}

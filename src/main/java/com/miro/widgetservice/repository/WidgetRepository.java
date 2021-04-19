package com.miro.widgetservice.repository;

import com.miro.widgetservice.dto.SearchAreaDto;
import com.miro.widgetservice.model.Widget;
import java.util.List;
import java.util.Optional;

public interface WidgetRepository {

    Optional<Widget> findById(Long id);

    List<Widget> findAll();

    List<Widget> findAll(Integer page, Integer size, SearchAreaDto searchAreaDto);

    Widget save(Widget widget);

    void deleteById(Long id);

    boolean isExist(Long id);

    void deleteAll();

    List<Widget> saveAll(List<Widget> widgetList);

    List<Widget> findAll(SearchAreaDto searchAreaDto);
}

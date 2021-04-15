package com.miro.widgetservice.repository.jpa;

import com.miro.widgetservice.model.WidgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WidgetJpaRepository extends JpaRepository<WidgetEntity, Long> {
}

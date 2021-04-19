package com.miro.widgetservice.repository.jpa;

import com.miro.widgetservice.dto.SearchAreaDto;
import com.miro.widgetservice.model.WidgetEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WidgetJpaRepository extends JpaRepository<WidgetEntity, Long> {

    @Modifying
    @Query("update WidgetEntity w set w.zIndex = w.zIndex + 1, w.modificationDate = current_timestamp "
        + "where w.zIndex >=:zIndexStart and w.zIndex <= :zIndexEnd")
    Integer incrementFromIndexToIndex(Integer zIndexStart, Integer zIndexEnd);

    @Query("select w from WidgetEntity w where w.zIndex >= :zIndex order by w.zIndex desc")
    List<WidgetEntity> findAllWithEqualOrGreaterZIndex(Integer zIndex);

    @Query("select max(w.zIndex) FROM WidgetEntity w")
    Integer findMaxZIndex();

    @Query("select w from WidgetEntity w where w.zIndex = (select  max(wi.zIndex) from WidgetEntity  wi )")
    WidgetEntity findWidgetWithMaxZIndex();

    boolean existsByzIndex(Integer zIndex);

    @Query("select w from WidgetEntity w where ((:#{#area.xPoint1} <= w.xPoint) "
        + "and (:#{#area.xPoint2} >= w.xPoint2)) "
        + "and ((:#{#area.yPoint1} <= w.yPoint) "
        + "and (:#{#area.yPoint2} >= w.yPoint2)) order by w.zIndex asc")
    List<WidgetEntity> findAll(@Param("area") SearchAreaDto searchAreaDto);

    @Query(value = "select w from WidgetEntity w where ((:#{#area.xPoint1} <= w.xPoint) "
        + "and (:#{#area.xPoint2} >= w.xPoint2)) "
        + "and ((:#{#area.yPoint1} <= w.yPoint) "
        + "and (:#{#area.yPoint2} >= w.yPoint2))")
    List<WidgetEntity> findAll(@Param("area") SearchAreaDto searchAreaDto, Pageable pageable);
}

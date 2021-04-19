package com.miro.widgetservice.util;

import com.miro.widgetservice.dto.SearchAreaDto;
import com.miro.widgetservice.exception.WidgetServiceException;
import java.util.Collection;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class WidgetUtil {

    public Integer getLastZIndexWithoutGap(Collection<Integer> integers) {
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

    public boolean isSearchDtoValid(SearchAreaDto searchAreaDto) {
        return searchAreaDto.getXPoint1() != null &&
            searchAreaDto.getYPoint1() != null &&
            searchAreaDto.getXPoint2() != null &&
            searchAreaDto.getYPoint2() != null;
    }
}

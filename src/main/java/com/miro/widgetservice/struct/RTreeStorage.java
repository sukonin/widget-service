package com.miro.widgetservice.struct;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.miro.widgetservice.dto.SearchAreaDto;
import com.miro.widgetservice.model.Widget;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RTreeStorage {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    private RTree<Widget, Geometry> coordinateTree = RTree.create();

    private Map<Long, Geometry> geometryMap = new HashMap<>();

    public void putOrReplace(Widget widget) {
        writeLock.lock();
        try {
            deleteIfExist(widget);

            float x1Point = widget.getXPoint().floatValue();
            float y1Point = widget.getYPoint().floatValue();
            float x2Point = x1Point + widget.getWidth();
            float y2Point = y1Point + widget.getHeight();

            Rectangle rectangle = Geometries.rectangle(x1Point, y1Point, x2Point, y2Point);

            coordinateTree = coordinateTree.add(widget, rectangle);
            geometryMap.put(widget.getId(), rectangle);
        } finally {
            writeLock.unlock();
        }
    }

    public void deleteIfExist(Widget widget) {
        Geometry geometry = geometryMap.get(widget.getId());
        if (geometry != null) {
            coordinateTree = coordinateTree.delete(widget, geometry);
            geometryMap.remove(widget.getId());
        }
    }

    public List<Widget> findInArea(SearchAreaDto searchAreaDto) {
        readLock.lock();
        try {
            Rectangle rectangle = Geometries.rectangle(searchAreaDto.getXPoint1(),
                searchAreaDto.getYPoint1(),
                searchAreaDto.getXPoint2(),
                searchAreaDto.getYPoint2());

            return coordinateTree.search(rectangle)
                .map(Entry::value)
                .filter(w -> isInArea(searchAreaDto, w))
                .sorted((w1, w2) -> w1.getZIndex().compareTo(w2.getZIndex()))
                .toList()
                .toBlocking()
                .first();
        } finally {
            readLock.unlock();
        }
    }

    public void deleteAll() {
        writeLock.lock();
        try {
            geometryMap = new HashMap<>();
            coordinateTree = RTree.create();
        } finally {
            writeLock.unlock();
        }
    }

    private boolean isInArea(SearchAreaDto searchAreaDto, Widget widget) {
        float x1Point = widget.getXPoint();
        float y1Point = widget.getYPoint();
        float x2Point = x1Point + widget.getWidth();
        float y2Point = y1Point + widget.getHeight();

        return searchAreaDto.getXPoint1() <= x1Point
            && searchAreaDto.getXPoint2() >= x2Point
            && searchAreaDto.getYPoint1() <= y1Point
            && searchAreaDto.getYPoint2() >= y2Point;
    }
}

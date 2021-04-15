package com.miro.widgetservice.repository;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdGenerator {

    private static final AtomicLong atomicIdGenerator = new AtomicLong();

    public long generateNextId() {
        return atomicIdGenerator.incrementAndGet();
    }
}

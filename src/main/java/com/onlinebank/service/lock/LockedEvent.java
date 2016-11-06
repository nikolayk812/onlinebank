package com.onlinebank.service.lock;

import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.concurrent.locks.Lock;

class LockedEvent extends ApplicationEvent {
    private final List<Lock> locks;

    LockedEvent(Object source, List<Lock> locks) {
        super(source);
        this.locks = locks;
    }

    List<Lock> getLocks() {
        return locks;
    }
}

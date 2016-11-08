package com.onlinebank.service.lock;

import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.concurrent.locks.Lock;

import static java.util.Objects.requireNonNull;

class LockedEvent extends ApplicationEvent {
    private final List<Lock> locks;

    LockedEvent(Object source, List<Lock> locks) {
        super(source);
        requireNonNull(locks);
        Preconditions.checkArgument(!locks.contains(null));
        this.locks = locks;
    }

    List<Lock> getLocks() {
        return locks;
    }
}

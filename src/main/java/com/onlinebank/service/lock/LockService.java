package com.onlinebank.service.lock;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.stream.Collectors.*;

/**
 *
 * @param <T>
 * @param <R>
 */
public class LockService<T extends Comparable<T>, R> {
    private static final Logger log = LoggerFactory.getLogger(LockService.class);

    //TODO: make weak?
    private final ConcurrentMap<T, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationEventPublisher publisher;


    /**
     * Sorts locks!
     *
     * @param keys
     * @param callable
     * @return
     */
    public final R runUnderWriteLocks(Collection<T> keys, Callable<R> callable) {
        Objects.requireNonNull(keys);
        Preconditions.checkArgument(!keys.contains(null), "Null lock keys are not allowed");
        Preconditions.checkArgument(keys.size() > 0);

        //TODO: validate if I am having read lock!
        List<T> keyList = new ArrayList<>(keys);
        List<Lock> locks = keyList.stream()
                .sorted()
                .map(this::writeLock)
                .collect(toList());
        locks.forEach(Lock::lock);
        try {
            return callable.call();
        } catch (RuntimeException e) {
            log.warn("Exception for keys {}: {}", keys, e.toString());
            throw e;
        } catch (Exception e) {
            log.warn("Exception for keys {}: {}", keys, e.toString());
            throw new RuntimeException(e);
        } finally {
            //TODO: async or not?
            publisher.publishEvent(new LockedEvent(this, locks));
        }
    }

    /**
     *
     * @param keys
     * @param callable
     * @return
     */
    //TODO: avoid copy-paste
    public final R runUnderReadLocks(Collection<T> keys, Callable<R> callable) {
        Objects.requireNonNull(keys);
        Preconditions.checkArgument(!keys.contains(null), "Null lock keys are not allowed");
        Preconditions.checkArgument(keys.size() > 0);

        List<T> keyList = new ArrayList<>(keys);
        List<Lock> locks = keyList.stream()
                .sorted()
                .map(this::readLock)
                .collect(toList());
        locks.forEach(Lock::lock);
        try {
            return callable.call();
        } catch (RuntimeException e) {
            log.warn("Exception for keys {}: {}", keys, e.toString());
            throw e;
        } catch (Exception e) {
            log.warn("Exception for keys {}: {}", keys, e.toString());
            throw new RuntimeException(e);
        } finally {
            //TODO: async or not?
            publisher.publishEvent(new LockedEvent(this, locks));
        }
    }

    private Lock readLock(T key) {
        return lockMap.computeIfAbsent(key, l -> new ReentrantReadWriteLock()).readLock();
    }

    private Lock writeLock(T key) {
        return lockMap.computeIfAbsent(key, l -> new ReentrantReadWriteLock()).writeLock();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    private void unlockAllFromEvent(LockedEvent event) {
        log.debug("Releasing {} locks", event.getLocks().size());
        event.getLocks().forEach(Lock::unlock);
    }

}

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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Locking service
 * Supports locking of application critical entities to prevent concurrent update.
 * Locks are taken in natural order to prevent deadlock.
 *
 * Reads would need no lock, tx would read READ_COMMITED data
 *
 * @param <K> type of lock key
 * @param <R> type of return type
 */
public class LockService<K extends Comparable<K>, R> {
    private static final Logger log = LoggerFactory.getLogger(LockService.class);

    private final ConcurrentMap<K, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * Calls code from {@link Callable} under locks taken in natural order
     * Locks are computed according to lock keys.
     *
     * @param keys lock keys
     * @param callable callable
     * @return result from callable
     */
    public final R callUnderUpdateLocks(Collection<K> keys, Callable<R> callable) {
        requireNonNull(keys);
        requireNonNull(callable);
        Preconditions.checkArgument(!keys.contains(null), "Null lock keys are not allowed");
        Preconditions.checkArgument(keys.size() > 0);

        List<K> keyList = new ArrayList<>(keys);
        List<Lock> locks = keyList.stream()
                .sorted()
                .map(this::getLock)
                .collect(toList());
        locks.forEach(Lock::lock);
        log.debug("Acquired {} locks", locks.size());
        try {
            return callable.call();
        } catch (RuntimeException e) {
            log.warn("Exception for keys {}: {}", keys, e.toString());
            throw e;
        } catch (Exception e) {
            log.error("Exception for keys {}: {}", keys, e.toString());
            throw new RuntimeException(e);
        } finally {
            publisher.publishEvent(new LockedEvent(this, locks));
        }
    }

    private Lock getLock(K key) {
        return lockMap.computeIfAbsent(key, l -> new ReentrantLock());
    }

    //catches LockedEvent from publisher, releases locks AFTER transaction COMMIT or ROLLBACK, not earlier!
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    private void unlockAllFromEvent(LockedEvent event) {
        log.debug("Releasing {} locks", event.getLocks().size());
        event.getLocks().forEach(Lock::unlock);
    }

}

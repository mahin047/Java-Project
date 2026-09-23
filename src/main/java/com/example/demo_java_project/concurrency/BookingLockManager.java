package com.example.demo_java_project.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One ReentrantLock per resource. If two threads (inside THIS app instance)
 * try to book the same resource at the same time, the second one waits
 * until the first finishes — they never race each other into the database.
 */
public final class BookingLockManager {

    private static final ConcurrentHashMap<Integer, ReentrantLock> locks = new ConcurrentHashMap<>();

    private BookingLockManager() { }

    private static ReentrantLock lockFor(int resourceId) {
        return locks.computeIfAbsent(resourceId, id -> new ReentrantLock());
    }

    /** Runs work while holding the lock for this resource id. */
    public static <T> T runLocked(int resourceId, Callable<T> work) throws Exception {
        ReentrantLock lock = lockFor(resourceId);
        lock.lock();
        try {
            return work.call();
        } finally {
            lock.unlock();
        }
    }
}
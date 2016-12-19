package com.softwareverde.util.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class StacktraceUtil {
    static void printStackTrace(final StackTraceElement[] stacktraceElements) {
        String stacktrace = "";
        for (final StackTraceElement stackTraceElement : stacktraceElements) {
            stacktrace += stackTraceElement.toString() +"\n";
        }
        System.out.println(stacktrace);
    }
}

class ReportableReentrantLock extends ReentrantLock {
    public void printOwnerThread() {
        StacktraceUtil.printStackTrace(super.getOwner().getStackTrace());
    }
}

public class MultiLock<T> {
    private final ConcurrentHashMap<T, ReportableReentrantLock> _lockedItems = new ConcurrentHashMap<T, ReportableReentrantLock>();

    public void lock(final T... requiredObjects) {
        final List<ReportableReentrantLock> acquiredLocks = new ArrayList<ReportableReentrantLock>();

        Boolean allLocksWereAcquired = false;
        while (! allLocksWereAcquired) {
            allLocksWereAcquired = true;
            synchronized (_lockedItems) {
                for (final T object : requiredObjects) {
                    ReportableReentrantLock lock = _lockedItems.get(object);
                    if (lock == null) {
                        lock = new ReportableReentrantLock();
                        lock.lock();
                        _lockedItems.put(object, lock);
                        acquiredLocks.add(lock);
                    }
                    else {
                        Boolean lockWasAcquired = false;
                        try {
                            lockWasAcquired = lock.tryLock(20L, TimeUnit.MILLISECONDS);
                        }
                        catch (final InterruptedException e) { }

                        if (lockWasAcquired) {
                            acquiredLocks.add(lock);
                        }
                        else {
                            allLocksWereAcquired = false;
                            break;
                        }
                    }
                }

                if (! allLocksWereAcquired) {
                    for (final ReportableReentrantLock lock : acquiredLocks) {
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                }
            }

            if (! allLocksWereAcquired) {
                try { Thread.sleep(20L); } catch (final Exception e) { }
            }
        }
    }

    public void unlock(final T... releasedObjects) {
        for (final T releasedObject : releasedObjects) {
            final ReentrantLock lock = _lockedItems.get(releasedObject);
            if (lock != null) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}

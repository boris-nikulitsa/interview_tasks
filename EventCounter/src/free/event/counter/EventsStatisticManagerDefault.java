package free.event.counter;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @since 13 December 2015
 * @author Boris Nikulitsa
 */
class EventsStatisticManagerDefault implements EventsStatisticManager {
    
    private static final long MILLISECONDS_IN_MINUTE = 60 * 1000;
    private static final long MILLISECONDS_IN_HOUR = 60 * 60 * 1000;
    private static final long MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;
    
    private final EventsStatisticStore store;
    private final StatisticCache cache;
    
    public EventsStatisticManagerDefault(EventsStatisticStore store) {
        if (store == null) {
            throw new IllegalArgumentException("Expected store instance");
        }
        this.store = store;
        this.cache = new StatisticCache(store);
    }

    @Override
    public void handleEventOccurrence() throws Exception {
        // it's server time and not client time
        // probably it makes sense to think about more flexible handling of the time later
        // but for now we don't create unneded complexity and try to keep code simple
        cache.registerOccurrence(new Date().getTime());
    }

    @Override
    public long getEventsCountForPastDay() throws Exception {
        long toTime = new Date().getTime();
        return cache.countEvents(toTime - MILLISECONDS_IN_DAY, toTime);
    }

    @Override
    public long getEventsCountForPastHour() throws Exception {
        long toTime = new Date().getTime();
        return cache.countEvents(toTime - MILLISECONDS_IN_HOUR, toTime);
    }

    @Override
    public long getEventsCountForPastMinute() throws Exception {
        long toTime = new Date().getTime();
        return cache.countEvents(toTime - MILLISECONDS_IN_MINUTE, toTime);
    }
    
    
    private static final class StatisticCache {
        /*
         * Makes sense to do option configurable if we want to prevent 
         * fight for resources with more valuable parts of the software.
         */
        private static final int MAX_SIZE = 1073741824; // 2 ^ 31
        // Please assign true for printPerformanceDiagnostic to print diagnostic which we require to deal with preformance issues 
        // We use System.out instead of classical logging because it's example program and not production code :) But we should improve it in case production code
        // And don't forget about logging preasure on FS ! We must be carefull and provide flexible logging configuration in case java logging for performance issues :)
        private static final boolean printPerformanceDiagnostic = true;

        private long[] statistic = new long[1024]; // last minute statistic
        private int index = -1;
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private final EventsStatisticStore store;
        
        StatisticCache(EventsStatisticStore store) {
            this.store = store;
        }
        
        /**
         * @param time - the number of milliseconds since January 1, 1970, 00:00:00 GMT
         */
        void registerOccurrence(long time) throws Exception {
            int newSizeReached = -1;

            lock.writeLock().lock();
            try {
                storePassedMinutes(time);
                if (index + 1 == statistic.length) {
                    newSizeReached = index + 1; // move tracing out of the write lock to prevent dead locks. It's better to be double safe :)

                    if (index + 1 == MAX_SIZE) {
                        throw new IllegalStateException("Reached allowed maximum of memory consumption");
                    }

                    // reallocation -> we will reach amortized perfomance during add operation
                    // We can use potential method to understand performance complexity :)
                    // OTOH most important is that there's System.arraycopy happens and it's native operation. It means that it works faster than other operations
                    // Actually similar idea is for binary operations :) We deal with 2bit hardware which means that binary operations are fast 
                    int newSize = statistic.length << 1; // * 2
                    statistic = Arrays.copyOf(statistic, newSize);
                }
                statistic[++index] = time;
            } finally {
                lock.writeLock().unlock();
                if (printPerformanceDiagnostic && newSizeReached != -1) {
                    System.out.println("Reached " + newSizeReached + " events during last minute");
                }
            }
        }

        private void storePassedMinutes(long currentTime) throws Exception {
            lock.writeLock().lock();
            try {
                if (index >= 0 && (statistic[index] / 60000) < (currentTime / 60000)) {
                    // new minute started and it makes sense to do operation
                    if (printPerformanceDiagnostic) {
                        System.out.println("Store statistic for passed minute. Size=" + index+1);
                    }
                    long[] passedMinuteStats = Arrays.copyOf(statistic, index + 1);
                    store.store(passedMinuteStats);
                    
                    statistic = new long[1024];
                    index = -1;
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        /**
         * @param fromTime - the number of milliseconds since January 1, 1970, 00:00:00 GMT
         * @param toTime - the number of milliseconds since January 1, 1970, 00:00:00 GMT
         * @return number of events occurred from fromTime to toTime
         */
        long countEvents(long fromTime, long toTime) throws Exception {
            if (fromTime > toTime) {
                return 0;
            }

            long eventsCount = 0;
            lock.readLock().lock();
            try {
                if (index >= 0) {
                    eventsCount += EventCounterUtils.calculateCount(fromTime, toTime, statistic, index + 1);
                    if (fromTime / 60_000 == statistic[0] / 60_000) {
                        // no need to look into store
                        return eventsCount;
                    }
                }
            } finally {
                lock.readLock().unlock();
            }
            
            eventsCount += store.countEvents(fromTime, toTime);
            return eventsCount;
        }
    }
}

package free.event.counter;

/**
 * @since 13 December 2015
 * @author Boris Nikulitsa
 */
public interface EventsStatisticStore {
    
    /**
     * @param statistics - statistic per minute
     * @throws Exception if operation failed
     */
    void store(long[] statistics) throws Exception;
    
    /**
     * Calculates number of events registered in the store for the passed interval. 
     * Time passed as the number of milliseconds since January 1, 1970, 00:00:00 GMT.
     * 
     * @param fromTime - left position of time interval
     * @param toTime - right position of time interval
     * @return number of events
     * @throws Exception if operation failed
     */
    long countEvents(long fromTime, long toTime) throws Exception;
    
}

package free.event.counter;

/**
 * @since 13 December 2015
 * @author Boris Nikulitsa
 */
public interface EventsStatisticManager {
    
    /**
     * maximum load is 10k events per second
     */
    void handleEventOccurrence() throws Exception;
    
    long getEventsCountForPastDay() throws Exception ;
    
    long getEventsCountForPastHour() throws Exception ;
    
    long getEventsCountForPastMinute() throws Exception ;
    
}

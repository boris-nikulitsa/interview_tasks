package free.event.counter;

/**
 * ApplicationContext encapsulates application configuration logic. 
 * It could be changed to support configuration through xml file, java system properties or something else.
 * 
 * @since 13 December 2015
 * @author Boris Nikulitsa
 */
public class ApplicationContext {
    
    private static final EventsStatisticStore ess = new EventsStatisticFSStore("D:\\boris-nikulitsa\\eventstore_storage"); // not elegant, so please improve it for correct path
    private static final EventsStatisticManager esm = new EventsStatisticManagerDefault(ess);
    
    public static EventsStatisticManager getEventsStaticManagerInstance() {
        return esm;
    }
    
    public static EventsStatisticStore getEventsStatisticStore() {
        return ess;
    }
}

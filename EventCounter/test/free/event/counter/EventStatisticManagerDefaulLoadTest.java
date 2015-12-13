package free.event.counter;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * It is not a unit test and it's some kind of combined integration + performance test. 
 * It isn't feasible to write something elegant in case limited time. 
 * Nobody wants to miss cinema session at evening :) Sunday is good day to rest :)
 * 
 * TODO makes sense to develop better tests
 * 
 * @since 13 December 2015
 * @author Boris Nikulitsa
 */
public class EventStatisticManagerDefaulLoadTest {
    
    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(120);
        for (int i=0; i<100; ++i) {
            service.execute(new EventRegisterTask());
        }
        for (int i=0; i<20; ++i) {
            service.execute(new EventCounterTask());
        }
        
        try {
            Thread.sleep(3 * 60 * 1000);
            
            service.shutdown();
        } catch (InterruptedException e) {
            service.shutdownNow();
        }
        
        try {
            if (!service.isTerminated() && !service.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("Cannot shutdown working tasks. There's something wrong");
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(EventStatisticManagerDefaulLoadTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static class EventCounterTask implements Runnable {
        private final EventsStatisticManager esm;

        public EventCounterTask() {
            this.esm = ApplicationContext.getEventsStaticManagerInstance();
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    System.out.println("For past minutes : " + esm.getEventsCountForPastMinute());
                    System.out.println("For past hour: " + esm.getEventsCountForPastHour());
                    System.out.println("For past day: " + esm.getEventsCountForPastDay());
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
    }
    
    private static class EventRegisterTask implements Runnable {
        private final EventsStatisticManager esm;
        private final AtomicLong loadDetector = new AtomicLong();
        private final long startTime = (new Date()).getTime();

        public EventRegisterTask() {
            this.esm = ApplicationContext.getEventsStaticManagerInstance();
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    long secondIndex = (new Date().getTime() - startTime) / 60_000 + 1;
                    if (loadDetector.get() <= 100 * secondIndex) {
                        // maximum load doesn't reached
                        loadDetector.incrementAndGet();
                        esm.handleEventOccurrence();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}

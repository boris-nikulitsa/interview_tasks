package free.event.counter;

import java.util.Date;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @since 13 December 2015
 * @author Boris Nikulitsa
 */
public class EventsStatisticFSStoreTest {
    
    private long toTime;
    
    @BeforeClass
    public void setUp() {
        toTime = new Date().getTime();
        toTime = toTime - toTime % 60000 + 25000; // we have 25 seconds during last minute
    }
    
    @Test
    public void testStore() throws Exception {
        long[][] values = new long[][] {
            new long[] {toTime - 24000, toTime - 12000, toTime-3000, toTime-3000, toTime-3000, toTime-3000, toTime-3000, toTime, toTime, toTime, toTime, toTime, toTime, toTime + 12000, toTime + 13000},
            new long[] {toTime-65000, toTime-61000, toTime-60000, toTime - 55000, toTime - 54000, toTime - 54000, toTime-54000},
            new long[] {toTime-20*60000-12000,toTime-20*60000-10000,toTime-20*60000-9000,toTime-20*60000+12003,toTime-20*60000+12003,toTime-20*60000+12004},
            new long[] {toTime-40*60000-12000,toTime-40*60000-10000,toTime-40*60000-9000,toTime-40*60000+12003,toTime-40*60000+12003},
            new long[] {toTime-58*60000-12000,toTime-58*60000-10000,toTime-58*60000-9000,toTime-58*60000+11003,toTime-58*60000+11303,toTime-58*60000+11404},
            new long[] {toTime-59*60000-12000,toTime-59*60000-10000,toTime-59*60000-9000,toTime-59*60000+12003,toTime-59*60000+12004},
            new long[] {toTime-60*60000-12000,toTime-60*60000-10000,toTime-60*60000+12003,toTime-60*60000+12004},
            new long[] {toTime-61*60000-12000,toTime-61*60000-12000,toTime-61*60000-12000,toTime-61*60000-10000,toTime-61*60000-9000,toTime-61*60000+12003,toTime-61*60000+12003,toTime-61*60000+12004},
            new long[] {toTime - 24000 - 24*60*60*1000, toTime - 12000 - 24*60*60*1000, toTime-3000 - 24*60*60*1000, toTime-3000 - 24*60*60*1000, toTime-3000 - 24*60*60*1000, toTime-3000 - 24*60*60*1000, toTime-3000 - 24*60*60*1000, toTime - 24*60*60*1000, toTime - 24*60*60*1000, toTime + 13000 - 24*60*60*1000},
            new long[] {toTime - 24000 - 2*24*60*60*1000, toTime - 12000 - 2*24*60*60*1000, toTime-3000 - 2*24*60*60*1000, toTime-3000 - 2*24*60*60*1000, toTime-3000 - 2*24*60*60*1000, toTime-3000 - 2*24*60*60*1000, toTime-3000 - 2*24*60*60*1000, toTime - 2*24*60*60*1000, toTime - 2*24*60*60*1000, toTime + 13000 - 2*24*60*60*1000},
            new long[] {toTime - 24000 - 3*24*60*60*1000, toTime - 12000 - 3*24*60*60*1000, toTime-3000 - 3*24*60*60*1000, toTime-3000 - 3*24*60*60*1000, toTime-3000 - 3*24*60*60*1000, toTime-3000 - 3*24*60*60*1000, toTime-3000 - 3*24*60*60*1000, toTime - 3*24*60*60*1000, toTime - 3*24*60*60*1000, toTime + 13000 - 3*24*60*60*1000}
        };
        for (long[] stats: values) {
            ApplicationContext.getEventsStatisticStore().store(stats);
        }
    }
    
    @Test(dependsOnMethods = { "testStore" })
    public void testCountEventsForPastMinute() throws Exception {
        Assert.assertEquals(ApplicationContext.getEventsStatisticStore().countEvents(toTime - 60 * 1000, toTime), 18);
        Assert.assertEquals(ApplicationContext.getEventsStatisticStore().countEvents(toTime - 60 * 1000 + 1, toTime + 1), 17);
        Assert.assertEquals(ApplicationContext.getEventsStatisticStore().countEvents(toTime - 60 * 1000 - 1, toTime - 1), 12);
    }
    
    @Test(dependsOnMethods = { "testCountEventsForPastMinute" })
    public void testCountEventsForPastHour() throws Exception {
        Assert.assertEquals(ApplicationContext.getEventsStatisticStore().countEvents(toTime - 60 * 60 * 1000, toTime), 44);
        Assert.assertEquals(ApplicationContext.getEventsStatisticStore().countEvents(toTime - 60 * 60 * 1000-1, toTime-1), 38);
        Assert.assertEquals(ApplicationContext.getEventsStatisticStore().countEvents(toTime - 60 * 60 * 1000+12000, toTime+12000), 45);
        Assert.assertEquals(ApplicationContext.getEventsStatisticStore().countEvents(toTime - 60 * 60 * 1000+12004, toTime+12004), 44);
    }
    
    @Test(dependsOnMethods = { "testCountEventsForPastHour" })
    public void testCountEventsForPastDay() throws Exception {
        Assert.assertEquals(ApplicationContext.getEventsStatisticStore().countEvents(toTime - 24*60 * 60 * 1000, toTime), 57);
        Assert.assertEquals(ApplicationContext.getEventsStatisticStore().countEvents(toTime - 24*60 * 60 * 1000-1, toTime-1), 51);
    }
    
}

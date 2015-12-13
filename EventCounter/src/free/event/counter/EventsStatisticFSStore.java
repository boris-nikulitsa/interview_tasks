package free.event.counter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @since 13 December 2015
 * @author Boris Nikulitsa
 */
public class EventsStatisticFSStore implements EventsStatisticStore {
    
    private final Path configFolderPath;
    
    public EventsStatisticFSStore(String configFolderPath) {
        if (configFolderPath == null || configFolderPath.isEmpty()) {
            throw new IllegalArgumentException("Invalid configFolderPath passed: " + configFolderPath);
        }
        this.configFolderPath = Paths.get(configFolderPath);
        if (!Files.isDirectory(this.configFolderPath, LinkOption.NOFOLLOW_LINKS)) { 
            // for now we don't allow advanced FS configuration
            // makes sense to keep code simple
            // we can think about more cases later 
            throw new IllegalArgumentException("Invalid configFolderPath passed: " + configFolderPath);
        }
        if (!Files.isReadable(this.configFolderPath)) {
            throw new IllegalArgumentException("Read rights required for " + configFolderPath);
        }
        if (!Files.isWritable(this.configFolderPath)) {
            throw new IllegalArgumentException("Write rights required for " + configFolderPath);
        }
    }
    
    @Override
    public void store(long[] statisticPerMinute) throws IOException {
        // 1. we don't modify files for better performance
        // 2. we try to organize good structure for fast analysis
        String fileName = String.valueOf(statisticPerMinute[0] / 60000);
        File minuteStatsFile = new File(configFolderPath.toString(), fileName);
        if (minuteStatsFile.exists()) {
            throw new IOException("File System corrupted. Found duplicate of " + minuteStatsFile.getAbsolutePath());
        }
            
        // don't think about memory limitations for now
        // memory overhead is 10 000 * 60 * 8 ~ 5Mb isn't critical, but it makes sense to optimize it later
        ByteBuffer bufferWrapper = ByteBuffer.allocate(statisticPerMinute.length * 8);
        for (long val: statisticPerMinute) {
            bufferWrapper.putLong(val);
        }
        byte[] buffer = bufferWrapper.array();
            
        try (FileOutputStream out = new FileOutputStream(minuteStatsFile)) {
            out.write(buffer);
        }
    }

    @Override
    public long countEvents(long fromTime, long toTime) throws IOException {
        long last = toTime / 60000 - 1;
        long index = fromTime / 60000 + 1;
        long count = 0;
        while (index <= last) {
            File f = new File(configFolderPath.toFile(), String.valueOf(index));
            long fileLength = f.exists() ? f.length() : 0;
            if (fileLength % 8 != 0) {
                // I didn't work much with this code before, but I don't have time to do good investigation about it now
                // TODO: So it makes sense to add assertion and google about it more later
                throw new IllegalStateException("Unexpected file length " + f.getAbsolutePath() + " : " + fileLength);
            }
            count += fileLength / 8;
            ++index;
        }
        
        long[] statsPerMinute = read(fromTime);
        if (statsPerMinute.length > 0) {
            count += EventCounterUtils.calculateCount(fromTime, toTime, statsPerMinute, statsPerMinute.length);
        }
        if (toTime / 60000 != fromTime / 60000) {
            statsPerMinute = read(toTime);
            if (statsPerMinute.length > 0) {
                count += EventCounterUtils.calculateCount(fromTime, toTime, statsPerMinute, statsPerMinute.length);
            }
        }
        return count;
    }
    
    private long[] read(long theTime) throws IOException {
        File f = new File(configFolderPath.toFile(), String.valueOf(theTime / 60000));
        if (!f.exists()) {
            return new long[0];
        }
        
        // don't think about memory limitations for now
        // memory overhead is 10 000 * 60 * 8 ~ 5Mb isn't critical, but it makes sense to optimize it later
        int fileLength = (int)f.length();
        if (fileLength % 8 != 0) {
            // I didn't work much with this code before, but I don't have time to do good investigation about it now
            // TODO: So it makes sense to add assertion and google about it more later
            throw new IllegalStateException("Unexpected file length " + f.getAbsolutePath() + " : " + fileLength);
        }
        
        byte[] buffer = new byte[fileLength];
        try (FileInputStream in = new FileInputStream(f)) {
            int readCount = in.read(buffer);
            if (readCount != fileLength) {
                // I didn't work much with this code before, but I don't have time to do good investigation about it now
                // TODO: So it makes sense to add assertion and google about it more later
                throw new IOException("File length isn't correct: " + f.getAbsolutePath() + " : " + fileLength);
            }
        }
        
        // TODO: makes sense to double check about memory issues from ByteBuffer
        // don't have time to do that now. *** happens :)
        ByteBuffer bufferWrapper = ByteBuffer.wrap(buffer);
        long[] statistic = new long[fileLength / 8];
        for (int i=0; i<statistic.length; ++i) {
            statistic[i] = bufferWrapper.getLong();
        }
        return statistic;
    }
    
}

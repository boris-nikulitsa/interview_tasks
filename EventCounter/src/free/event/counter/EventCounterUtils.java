package free.event.counter;

/**
 * @since 13 December 2015
 * @author Boris Nikulitsa
 */
final class EventCounterUtils {
    
    static int calculateCount(long leftPosition, long rightPosition, long[] values, int size) {
        if (values == null) {
            throw new IllegalArgumentException("values is null");
        }
        if (size > values.length) {
            throw new IllegalArgumentException("Invalid size '" + size + "' when values length is " + values.length);
        }
        if (size == 0) {
            throw new IllegalArgumentException("Empty size passed " + size);
        }
        if (leftPosition > rightPosition) {
            throw new IllegalArgumentException("Invalid arguments passeed " + leftPosition + " : " + rightPosition);
        }
        if (values[0] >= leftPosition && values[size - 1] <= rightPosition) {
            return size;
        }
        if (values[size - 1] < leftPosition || values[0] > rightPosition) {
            return 0;
        }
        int leftMostIndex = 0, rightMostIndex = values.length - 1;
        while (values[leftMostIndex] < leftPosition) {
            ++leftMostIndex;
        }
        while (values[rightMostIndex] > rightPosition) {
            --rightMostIndex;
        }
        return rightMostIndex - leftMostIndex + 1;
    }
    
}

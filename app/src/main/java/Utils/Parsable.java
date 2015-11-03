package Utils;

/**
 * Author: Louis Jenkins
 * <p/>
 * Interface used to allow the AsyncParser to parse out a new object, line by line.
 */
public interface Parsable<T> {
    T parseObject(String line, String delimiter);
}

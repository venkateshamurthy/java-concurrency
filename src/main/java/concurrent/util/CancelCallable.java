package concurrent.util;

import java.util.concurrent.Callable;

/**
 * Interface for cancellable tasks which extends {@link Callable}
 * 
 * @author vmurthy
 * 
 * @param <T> type of result from callable
 */
public interface CancelCallable<T> extends Callable<T> {
	public void cancel();
}
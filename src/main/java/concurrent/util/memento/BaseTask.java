package concurrent.util.memento;

import java.util.Map;
import java.util.concurrent.Callable;
/**
 * A task abstraction to deal with thread context to be saved and restores before and after task execution.
 * Here the {@link MDC} as a context collector is assumed in the adapted form of {@link Slf4JMDC}. 
 * @author murthyv
 *
 * @param <V>
 */
public abstract class BaseTask<T> {

	public static <T> Callable<T> wrap(Callable<T> callable,Map<String,String> ctx){
		return AbstractBaseTask.<T,String,String>wrap(callable, ctx, new Slf4JMDC());
	}

	public static Runnable wrap(Runnable runnable, Map<String,String> ctx) {
		return AbstractBaseTask.<String,String>wrap(runnable, ctx, new Slf4JMDC());
	}
}

package concurrent.util.contextual;

import java.util.concurrent.FutureTask;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

/**
 * A simple extension to {@link FutureTask} that captures and delivers
 * {@link ContextualRunnable} from the runnable passed. its basically intended
 * for {@link ContextualThreadPoolExecutor}.
 * 
 * @author murthyv
 *
 * @param <V>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class ContextualFutureTask<V> extends FutureTask<V> {
	
	/** ContextualRunnable reference captured during construction */
	ContextualRunnable<?> contextualRunnable;
	/**
	 * Constructor
	 * @param runnable
	 * @param result
	 */
	public ContextualFutureTask(Runnable runnable, V result) {
		super(runnable, result);
		contextualRunnable = 
				runnable instanceof ContextualRunnable 
				? (ContextualRunnable) runnable	: null;
	}
}
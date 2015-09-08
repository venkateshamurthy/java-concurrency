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
	 * Constructor.
	 * 
	 * @param runnable
	 *            - if the runnable passed is of {@link ContextualRunnable} then
	 *            it gets stored locally for later retrieval.
	 * @param result
	 *            the result to return on successful completion. If you don't
	 *            need a particular result, consider using constructions of the
	 *            form: Future<?> f = new FutureTask<Void>(runnable, null)
	 */
	public ContextualFutureTask(Runnable runnable, V result) {
		super(runnable, result);
		contextualRunnable = runnable instanceof ContextualRunnable ? (ContextualRunnable) runnable
				: null;
	}
}
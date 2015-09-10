package concurrent.util.contextual;



import java.util.concurrent.Callable;





import util.Util;

import com.google.common.base.Optional;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import concurrent.util.contextual.ContextualThreadPoolExecutor.ContextualThread;

/**
 * A Contextual wrapper for Callable.
 * 
 * @author murthyv
 *
 * @param <Context>
 */
@Slf4j
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContextualCallable<Context, V> implements Callable<V>, TaskContext<Context> {

	/** Context of callable */
	Context context;

	/** The actual core runnable that is decorated */
	Callable<V> callable;

	/** contextual thread */
	@NonFinal
	ContextualThread<Context> contextualThread;

	@NonFinal
	Throwable throwable;

	/**
	 * Set the context with a passed thread
	 * 
	 * @param contextualThread
	 */
	void setThreadContext(ContextualThread<Context> contextualThread) {
		this.contextualThread = contextualThread;
		contextualThread.setContext(context);
	}

	void clearThreadContext() {
		if (contextualThread != null)
			contextualThread.clearContext();
	}

	/** {@inheritDoc} */
	@Override
	public V call() throws Exception {
		try {
			return callable.call();
		} catch (Throwable t) {
			this.throwable = t;
			log.error("Error:", t);
			throw t;
		}
	}

	/**
	 * Decorates a callable with context
	 * 
	 * @param command
	 * @return ContextualCallable
	 */
	public static <Context, V> ContextualCallable<Context, V> make(
			final Callable<V> command) {
		return make(null, command);
	}

	/**
	 * Decorates a runnable with context. If command is an instance of
	 * {@link ContextualCallable} then return command as-is with proper
	 * casting.Else create a new instance with context abd command
	 * 
	 * @param context
	 * @param command
	 * @return ContextualRunnable
	 */
	public static <Context, V> ContextualCallable<Context, V> make(
			final Context context, final Callable<V> command) {
		ContextualCallable<Context, V> cc= 
				command instanceof ContextualCallable?(ContextualCallable<Context,V>)command:new ContextualCallable<Context, V>(context,command);
				//cast(command, new ContextualCallable<Context, V>(context,command));
		return cc;
	}

	/**
	 * A builder type method
	 * 
	 * @param context
	 *            to be set
	 * @return a new ContextualRunnable with passed context but with
	 *         {@link #callable}
	 */
	public ContextualCallable<Context, V> withContext(Context context) {
		return new ContextualCallable<Context, V>(context, callable);
	}

	/**
	 * discovering/un-earthing the {@code ContextualCallable} from passed
	 * {@link Callable callable}.
	 * <p>
	 * Note: Please check the result for non-null before using.
	 * 
	 * @param callable
	 *            an instance of {@link Callable}
	 * @return &lt;Context&gt; if {@code callable} passed can be discovered for
	 *         {@code ContextualCallable}/{@code ContextualFutureTask}.
	 *         otherwise a null.
	 */
	public static <Context, V> Context getContext(Callable<V> callable) {
		//callable != null && callable instanceof TaskContext	? ((TaskContext<Context>) callable).getContext() : null;
		TaskContext<Context> t=Util.<TaskContext<Context>>cast(callable);
		return t==null?null:t.getContext();
	}
	
}
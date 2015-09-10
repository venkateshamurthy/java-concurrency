package concurrent.util.contextual;

import static util.Util.cast;
import util.Util;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import concurrent.util.contextual.ContextualThreadPoolExecutor.ContextualThread;

/**
 * A Contextual wrapper for Runnable.
 * 
 * @author murthyv
 *
 * @param <Context>
 */
@Slf4j
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContextualRunnable<Context> implements Runnable,
		TaskContext<Context> {

	/** Context of runnable */
	Context context;

	/** The actual core runnable that is decorated */
	Runnable runner;

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
	public void run() {
		try {
			runner.run();
		} catch (Throwable t) {
			this.throwable = t;
			log.error("Error:", t);
		}
	}

	/**
	 * Decorates a runnable with context
	 * 
	 * @param command
	 * @return ContextualRunnable
	 */
	public static <Context> ContextualRunnable<Context> make(Runnable command) {
		return make(null, command);
	}

	/**
	 * Decorates a runnable with context. If command is an instance of
	 * {@link ContextualRunnable} then return command as-is with proper
	 * casting.Else create a new instance with context abd command
	 * 
	 * @param context
	 * @param command
	 * @return ContextualRunnable
	 */
	public static <Context> ContextualRunnable<Context> make(
			final Context context, final Runnable command) {
		return command instanceof ContextualRunnable ? (ContextualRunnable<Context>) command
				: new ContextualRunnable<Context>(context, command);
		// cast(command, new ContextualRunnable<Context>(context, command));
	}

	/**
	 * A builder type method
	 * 
	 * @param context
	 *            to be set
	 * @return a new ContextualRunnable with passed context but with
	 *         {@link #runner}
	 */
	public ContextualRunnable<Context> withContext(Context context) {
		return new ContextualRunnable<Context>(context, getRunner());
	}

	/**
	 * A utility method to return {@link ContextualRunnable} after
	 * discovering/un-earthing the {@code ContextualRunnable} from passed
	 * {@link Runnable runnable}.
	 * <p>
	 * Note: Please check the result for non-null before using.
	 * 
	 * @param runnable
	 *            an instance of {@link Runnable}
	 * @return {@code ContextualRunnable} possibly or null.
	 */
	public static <Context> Context getContext(Runnable runnable) {
		return runnable != null && runnable instanceof TaskContext 
				? ((TaskContext<Context>) runnable).getContext() : null;
		/*TaskContext<Context> t=Util.<TaskContext<Context>>cast(runnable);
		return t==null?null:t.getContext();*/
	}

}
package concurrent.util.contextual;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import concurrent.util.contextual.ContextualThreadPoolExecutor.ContextualThread;

/**
 * A Contextual wrapper for Runnable.
 * @author murthyv
 *
 * @param <Context>
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContextualRunnable<Context> implements Runnable {
	
	/** Context of runnable*/
	Context context;
	
	/** The actual core runnable that is decorated*/
	Runnable runner;
	
	@NonFinal ContextualThread<Context> contextualThread;
	
	/** Set the context with a passed thread 
	 * @param contextualThread
	 */
	void setThreadContext(ContextualThread<Context> contextualThread) {
		this.contextualThread=contextualThread;
		contextualThread.setContext(context);
	}
	
	void clearThreadContext() {
		if(contextualThread!=null)
		contextualThread.clearContext();
	}

	
	/** {@inheritDoc} */
	@Override
	public void run() {
		runner.run();
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
	 * Decorates a runnable with context. If command is an instance of {@link ContextualRunnable} 
	 * then return command as-is with proper casting.Else create a new instance with context abd command
	 * 
	 * @param context
	 * @param command
	 * @return ContextualRunnable
	 */
	public static <Context> ContextualRunnable<Context> make(
			final Context context, final Runnable command) {
		return ContextualRunnable.class.isAssignableFrom(command.getClass())
						? ((ContextualRunnable<Context>) command) 
								: new ContextualRunnable<Context>(context,command);
	}
	
	/**
	 * A builder type method
	 * @param context to be set
	 * @return a new ContextualRunnable with passed context but with {@link #runner}
	 */
	public ContextualRunnable<Context> withContext(Context context){
		return new ContextualRunnable<Context>(context,getRunner());
	}
	
	public static <Context> Context getContext(Runnable runnable){
		if(ContextualRunnable.class.isAssignableFrom(runnable.getClass())){
			return ((ContextualRunnable<Context>)runnable).getContext();
		}
		else return null;
	}
}
package concurrent.util.contextual;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
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
class ContextualFutureTask<Context, V> extends FutureTask<V> {

	/** Context reference captured during construction */
	Context context;
	
	/**
	 * Constructor.
	 * 
	 * @param runnable
	 *            - if the runnable passed is of {@link ContextualRunnable} then
	 *            its context gets stored locally for later retrieval.
	 * @param result
	 *            the result to return on successful completion. If you don't
	 *            need a particular result, consider using constructions of the
	 *            form: Future<?> f = new FutureTask<Void>(runnable, null)
	 * @throws NullPointerException if {@code runnable} is null
	 * @throws ClassCastException if {@code runnable} is not castable to {@code ContextualRunnable}           
	 */
	public ContextualFutureTask(Runnable runnable, V result) throws NullPointerException, ClassCastException{
		super(
				ContextualCallable
					.make(
							((ContextualRunnable<Context>) runnable).getContext(), 
							Executors.callable((ContextualRunnable<Context>) runnable, result)
						 )
			);
		context=((ContextualRunnable<Context>) runnable).getContext();
	}
	/**
	 * * Constructor.
	 * 
	 * @param callable
	 *            - if the callable passed is of {@link ContextualCallable} then
	 *            its context gets stored locally for later retrieval.
	 * @throws NullPointerException if {@code callable} is null
	 * @throws ClassCastException if {@code callable} is not castable to {@code ContextualCallable}           
            
	 */
	public ContextualFutureTask (Callable<V> callable)throws NullPointerException, ClassCastException{
		super(callable);
		context=((ContextualCallable<Context,V>) callable).getContext();
	}
}
package concurrent.util.contextual;

import java.util.Map;
import java.util.concurrent.Callable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;

/**
 * This is a {@link MDC} state/behaviour wrapper for the callable that requires
 * to manage the runnable context into the global per thread local by means of
 * {@link MDC}
 * 
 * @author murthyv
 *
 */
@Slf4j
@Data(staticConstructor = "of")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MDCCallable<V> implements Callable<V> {
	/** A Map of context key-value pairs */
	Map<String, String> context;// = MDC.getCopyOfContextMap();

	/** The callable that is decorated with MDC */
	Callable<V> callable;

	/** Detect any throwable from the run method */
	@NonFinal
	Throwable throwable;

	/**
	 * {@inheritDoc}.
	 * <p>
	 * This call method in specific sets the current context of this
	 * {@code runnable/callable} to the thread specific MDC before the actual
	 * run happens. <br>
	 * Subsequently the previous MDC context is set back after the callable has
	 * completed. The current and previous snapshot of callable context is
	 * managed somewhat akin to a memento.
	 */
	@Override
	public V call() throws Exception {
		@SuppressWarnings("unchecked")
		final Map<String, String> previous = MDC.getCopyOfContextMap();
		// Set the thread context
		setContext(context);
		// Next run the runner and within the runnable's run method MDC needs to
		// be accessed
		try {
			return callable.call();
		} catch (Throwable t) {
			log.error("Error encountered in MDCCallable", t);
			this.throwable = t;
			throw t;
		} finally {
			// swap the previous context back
			setContext(previous);
		}
	}

	/**
	 * sets the MDC Context passed. The MDC will be cleared if context is null.
	 * 
	 * @param context
	 *            is the map of context variables to be set.
	 */
	private void setContext(Map<?, ?> context) {
		if (context != null)
			MDC.setContextMap(context);
		else
			MDC.clear();
	}
}
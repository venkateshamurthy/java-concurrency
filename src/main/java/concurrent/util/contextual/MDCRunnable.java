package concurrent.util.contextual;

import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;

/**
 * This is a {@link MDC} state/behaviour wrapper for the runnable that requires
 * to manage the runnable context into the global per thread local by means of
 * {@link MDC}
 * 
 * @author murthyv
 *
 */
@Slf4j
@Data(staticConstructor = "of")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MDCRunnable implements Runnable {

	/** A Map of context key-value pairs */
	Map<String, String> context;// = MDC.getCopyOfContextMap();

	/** The runnable that is decorated with MDC */
	Runnable runnable;

	/** Detect any throwable from the run method */
	@NonFinal
	Throwable throwable;

	/**
	 * {@inheritDoc}.
	 * <p>
	 * This run method in specific sets the current context of this
	 * {@code runnable/callable} to the thread specific MDC before the actual
	 * run happens. <br>
	 * Subsequently the previous MDC context is set back after the runnable has
	 * completed. The current and previous snapshot of runnable context is
	 * managed somewhat akin to a memento.
	 */
	@Override
	public void run() {
		// Take the previous snapshot
		@SuppressWarnings("unchecked")
		final Map<String, String> previous = MDC.getCopyOfContextMap();
		// Set the thread context
		if (context != null)
			MDC.setContextMap(context);
		else
			MDC.clear();
		// Next run the runner and within the runnable's run method MDC needs to
		// be accessed
		try {
			runnable.run();
		} catch (Throwable t) {
			this.throwable = t;
			log.error("Error encountered in MDCRunnable", t);
		} finally {
			// swap the previous context back
			if (previous != null)
				MDC.setContextMap(previous);
			else
				MDC.clear();
		}
	}

}
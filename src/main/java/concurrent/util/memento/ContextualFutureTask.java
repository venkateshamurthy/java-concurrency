package concurrent.util.memento;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.slf4j.MDC;

/**
 * A small template based extension to {@link FutureTask} <br>
 * for storing and restoring the thread context as a memento adhering to the
 * interface {@link ContextualMementoOriginator}
 * 
 * @author murthyv
 *
 * @param <V>
 */
public class ContextualFutureTask<V> extends FutureTask<V> implements 
		ContextualMementoOriginator<Map<String, String>>,ContextualMemento<Map<String,String>> {

	/** A construction time injected context */
	private final ContextualMemento<Map<String, String>> thisMemento;
	/**
	 * The Constructor with callable context and callable.
	 * 
	 * @param callableContext
	 * @param callable
	 */
	public ContextualFutureTask(Map<String, String> callableContext,
			Callable<V> callable) {
		super(callable);
		thisMemento = MapMemento.create(callableContext);
	}

	/**
	 * The Constructor with runnable and runnable context.
	 * 
	 * @param runnableContext
	 * @param runnable
	 * @param result
	 */
	public ContextualFutureTask(Map<String, String> runnableContext,
			Runnable runnable, V result) {
		super(runnable, result);
		thisMemento = MapMemento.create(runnableContext);
	}

	/**
	 * {@inheritDoc} This is a template method that saves the existing context
	 * into a past memento to be later restored. After securing past memento
	 * then this threads context is applied before run.
	 */
	@Override
	public void run() {
		final ContextualMemento<Map<String, String>> pastMemento = saveToMemento();
		try {
			restoreFromMemento(thisMemento);
			super.run();
		} finally {
			restoreFromMemento(pastMemento);
		}
	}
	
	@Override
	public ContextualMemento<Map<String, String>> saveToMemento(
			Map<String, String> context) {
		return MapMemento.<String,String>create(context);
	}

	/**
	 * A simplified save memento operation where the implicit map is MDC context map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected ContextualMemento<Map<String, String>> saveToMemento() {
		return saveToMemento(MDC.getCopyOfContextMap());
	}

	@Override
	public void restoreFromMemento(
			ContextualMemento<Map<String, String>> memento) {
		if (memento.getContext().isEmpty())
			MDC.clear();
		else
			MDC.setContextMap(memento.getContext());
	}
	
	@Override
	public Map<String, String> getContext() {
		return thisMemento.getContext();
	}
	
}

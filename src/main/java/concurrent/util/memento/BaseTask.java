package concurrent.util.memento;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;
/**
 * A task abstraction to deal with thread context to be saved and restores before and after task execution.
 * @author murthyv
 *
 * @param <V>
 */
public abstract class BaseTask<V> implements
		ContextualMementoOriginator<Map<String, String>>,
		ContextualMemento<Map<String, String>> {

	/** A construction time injected context basically stored/retained as a memento*/
	protected final ContextualMemento<Map<String, String>> thisMemento;

	/** 
	 * Ctor 
	 * @param context represents a task specific context to be used when actual execution happens
	 */
	private BaseTask(Map<String, String> context) {
		thisMemento = saveToMemento(context);
	}

	@Override
	public Map<String, String> getContext() {
		return thisMemento.getContext();
	}

	/**
	 * {@inheritDoc} and return for to note it some where.
	 */
	@Override
	public ContextualMemento<Map<String, String>> saveToMemento(
			Map<String, String> context) {
		return MapMemento.<String, String> create(context);
	}

	/**
	 * A simplified save memento operation where the implicit map is MDC context
	 * map
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected ContextualMemento<Map<String, String>> saveToMemento() {
		return saveToMemento(MDC.getCopyOfContextMap());
	}

	/**
	 * {@inheritDoc}. In this implementation MDC is either set with memento context or cleared in case if memento is empty
	 */
	@Override
	public void restoreFromMemento(
			ContextualMemento<Map<String, String>> memento) {
		if (memento.getContext().isEmpty())
			MDC.clear();
		else
			MDC.setContextMap(memento.getContext());
	}
	
	/**
	 * Wraps a callable 
	 * @param ctx
	 * @param callable
	 * @return a {@link CallableTask}
	 */
	public static <V> Callable<V> wrap(Map<String, String> ctx,
			Callable<V> callable) {
		return new CallableTask<V>(callable, ctx);
	}
	
	/**
	 * Wraps a runnable
	 * @param ctx
	 * @param runnable
	 * @return {@link RunnableTask}
	 */
	public static Runnable wrap(Map<String, String> ctx, Runnable runnable) {
		return new RunnableTask(runnable, ctx);
	}
	/**
	 * A simple template wrapper in its call method to manage saving and restoring memento
	 * @author murthyv
	 *
	 * @param <V>
	 */
	private static class CallableTask<V> extends BaseTask<V> implements
			Callable<V> {
		private final Callable<V> callable;

		/** Ctor */
		private CallableTask(Callable<V> callable, Map<String, String> map) {
			super(map);
			this.callable = callable;
		}

		@Override
		public V call() throws Exception {
			if (callable == null)
				throw new IllegalStateException(
						"This method expects a valid callable to be set");
			final ContextualMemento<Map<String, String>> pastMemento = saveToMemento();
			try {
				restoreFromMemento(thisMemento);
				return callable.call();
			} finally {
				restoreFromMemento(pastMemento);
			}
		}
	}
	/**
	 * A simple template wrapper in its run method to manage saving and restoring memento
	 * @author murthyv
	 *
	 * @param <V>
	 */
	private static class RunnableTask extends BaseTask<Void> implements
			Runnable {
		/** Runnable */
		private final Runnable runnable;

		/** Ctor */
		private RunnableTask(Runnable runnable, Map<String, String> map) {
			super(map);
			this.runnable = runnable;
		}

		@Override
		public void run() {
			if (runnable == null)
				throw new IllegalStateException(
						"This method expects a valid runnable to be set");
			final ContextualMemento<Map<String, String>> pastMemento = saveToMemento();
			try {
				restoreFromMemento(thisMemento);
				runnable.run();
			} finally {
				restoreFromMemento(pastMemento);
			}
		}
	}

}

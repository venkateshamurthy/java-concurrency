package concurrent.util.memento;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;

public abstract class BaseTask<V> implements
		ContextualMementoOriginator<Map<String, String>>,
		ContextualMemento<Map<String, String>> {

	/** A construction time injected context */
	protected final ContextualMemento<Map<String, String>> thisMemento;

	/** Ctor */
	private BaseTask(Map<String, String> map) {
		thisMemento = MapMemento.create(map);
	}

	@Override
	public Map<String, String> getContext() {
		return thisMemento.getContext();
	}

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
	 * Restore from memento
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

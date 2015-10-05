package concurrent.util.memento;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.slf4j.MDC;
/**
 * A task abstraction to deal with thread context to be saved and restores before and after task execution.
 * The context collector can be such as MDC by means of adapted version of {@link MapContextCollection}
 *
 * @param <V>
 */
abstract class AbstractBaseTask<T, K, V> implements
		ContextualMementoOriginator<Map<K, V>>, ContextualMemento<Map<K, V>> {
	/**
	 * A construction time injected context basically stored/retained as a
	 * memento
	 */
	protected final ContextualMemento<Map<K, V>> thisMemento;
	/** COtext collector */
	protected final MapContextCollection<K, V> contextCollection;

	/**
	 * Ctor
	 * 
	 * @param context
	 *            represents a task specific context to be used when actual
	 *            execution happens
	 */
	private AbstractBaseTask(Map<K, V> context,
			MapContextCollection<K, V> contextCollection) {
		this.contextCollection = contextCollection;
		thisMemento = saveToMemento(context);
	}

	@Override
	public Map<K, V> getContext() {
		return thisMemento.getContext();
	}

	/**
	 * {@inheritDoc} and return for to note it some where.
	 */
	@Override
	public ContextualMemento<Map<K, V>> saveToMemento(Map<K, V> context) {
		return MapMemento.<K, V> create(context);
	}

	/**
	 * A simplified save memento operation where the implicit map is MDC context
	 * map
	 * 
	 * @return
	 */

	@SuppressWarnings("unchecked")
	protected ContextualMemento<Map<K, V>> saveToMemento() {
		return saveToMemento(contextCollection.getContextMap());
	}

	/**
	 * {@inheritDoc}. In this implementation MDC is either set with memento
	 * context or cleared in case if memento is empty
	 */
	@Override
	public void restoreFromMemento(ContextualMemento<Map<K, V>> memento) {
		Objects.requireNonNull(contextCollection);
		if (memento.getContext().isEmpty())
			contextCollection.clear();
		else
			contextCollection.setContextMap(memento.getContext());
	}

	/**
	 * Wraps a callable
	 * 
	 * @param ctx
	 * @param callable
	 * @return a {@link CallableTask}
	 */
	public static <T, K, V> Callable<T> wrap(Callable<T> callable,
			Map<K, V> ctx, MapContextCollection<K, V> contextCollection) {
		return new CallableTask<T, K, V>(callable, ctx, contextCollection);
	}

	/**
	 * Wraps a runnable
	 * 
	 * @param ctx
	 * @param runnable
	 * @return {@link RunnableTask}
	 */
	public static <K, V> Runnable wrap(Runnable runnable, Map<K, V> ctx,
			MapContextCollection<K, V> contextCollection) {
		return new RunnableTask<K, V>(runnable, ctx, contextCollection);
	}

	/**
	 * A simple template wrapper in its call method to manage saving and
	 * restoring memento
	 * 
	 * @author murthyv
	 *
	 * @param <V>
	 */
	private static class CallableTask<T, K, V> extends
			AbstractBaseTask<T, K, V> implements Callable<T> {
		/** A core callable to be set for decorating its call method */
		private final Callable<T> callable;

		/** Ctor */
		private CallableTask(Callable<T> callable, Map<K, V> map,
				MapContextCollection<K, V> contextCollection) {
			super(map, contextCollection);
			Objects.requireNonNull(callable,
					"This class expects a valid callable to be set");
			this.callable = callable;
		}

		/**
		 * {@inheritDoc}.This wrapper around the call method in here
		 * specifically acts as a template to <ll> <li>First save the existing
		 * {@link MDC} context perticular to the thread running to a past
		 * memento <li>Next, set the callable/runnable context passed during
		 * construction to this thread using MDC <li>Next, after the run is
		 * done; then restore past memento </ll>
		 */
		@Override
		public T call() throws Exception {
			final ContextualMemento<Map<K, V>> pastMemento = saveToMemento();
			try {
				restoreFromMemento(thisMemento);
				return callable.call();
			} finally {
				restoreFromMemento(pastMemento);
			}
		}
	}

	/**
	 * A simple template wrapper in its run method to manage saving and
	 * restoring memento
	 * 
	 * @author murthyv
	 *
	 * @param <V>
	 */
	private static class RunnableTask<K, V> extends
			AbstractBaseTask<Void, K, V> implements Runnable {
		/** A core runnable to be set for its run method to be decorated */
		private final Runnable runnable;

		/** Ctor */
		private RunnableTask(Runnable runnable, Map<K, V> map,
				MapContextCollection<K, V> contextCollection) {
			super(map, contextCollection);
			Objects.requireNonNull(runnable,
					"This class expects a valid runnable to be set");
			this.runnable = runnable;
		}

		/**
		 * {@inheritDoc}.This wrapper around the run method in here specifically
		 * acts as a template to <ll> <li>First save the existing {@link MDC}
		 * context perticular to the thread running to a past memento <li>Next,
		 * set the callable/runnable context passed during construction to this
		 * thread using MDC <li>Next, after the run is done; then restore past
		 * memento </ll>
		 */
		@Override
		public void run() {
			final ContextualMemento<Map<K, V>> pastMemento = saveToMemento();
			try {
				restoreFromMemento(thisMemento);
				runnable.run();
			} finally {
				restoreFromMemento(pastMemento);
			}
		}
	}

}

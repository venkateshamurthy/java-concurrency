package concurrent.util.contextual;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * DO NOT USE THIS YET....
 * <p>
 * A context aware thread pool executor.
 * <p>
 * This executor ensures that if a {@link ContextualRunnable context aware
 * runnable} is being used in conjunction <br>
 * with this executor; then &lt;Context&gt; is passed from
 * {@code ContextualRunnable} to the thread that runs it.
 * <p>
 * The thread pool however used in here is specialized
 * {@link ContextualThreadFactory} <br>
 * which creates a {@link ContextualThread} that understands
 * {@code ContextualRunnable}
 * 
 * @author murthyv
 *
 * @param <Context>
 *            represents a template/generic context variable
 */
@Slf4j
class ContextualThreadPoolExecutor<Context> extends ThreadPoolExecutor {
	/**
	 * Constructor - catch all constructor that will be called by invoked by
	 * other constructors(in super class).
	 * 
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param threadFactory
	 * @param rejectionHandler
	 * @see ContextualThreadFactory
	 */
	public ContextualThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			RejectedExecutionHandler rejectionHandler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, rejectionHandler);
	}

	/**
	 * {@inheritDoc}.
	 * <p>
	 * This method in this class specifically deals with
	 * {@code ContextualRunnable} whose context is set to the thread running the
	 * {@code Runnable}. <br>
	 * The method expects both {@link Runnable r} and {@link Thread t} to be
	 * {@code ContextualRunnable} and {@code ContextualThread} respectively.<br>
	 * The {@code Runnable r} is created by the user as
	 * {@code ContextualRunnable} however {@code Thread t} is manufactured by
	 * this thread pool and as {@code ContextualThread}.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		if (t instanceof ContextualThread) {
			ContextualRunnable<Context> ctxRun = ContextualRunnable
					.discoverFromRunnable(r);
			log.debug("beforeExecute:Thread t is of ContextualThread "
					+ "and whether r is contextRunnable:{}", ctxRun != null);
			if (ctxRun != null)
				ctxRun.setThreadContext((ContextualThread<Context>) t);
		}
		super.beforeExecute(t, r);
	}

	/**
	 * {@inheritDoc}.
	 * <p>
	 * This method additionally sets the running thread's context as null.
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable th) {
		super.afterExecute(r, th);
		if (r instanceof ContextualRunnable)
			((ContextualRunnable<?>) r).clearThreadContext();
	}

	public static <Context> ContextualThreadPoolExecutor<Context> newFixedThreadPool(
			int nThreads) {
		return new ContextualThreadPoolExecutor<Context>(nThreads, nThreads,
				0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
				new ContextualThreadFactory<Context>(), new AbortPolicy());
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return new ContextualFutureTask<T>(runnable, value);
	}

	/**
	 * Contextual wrapper for thread creation.
	 * 
	 * @author murthyv
	 *
	 * @param <Context>
	 */
	private static class ContextualThreadFactory<Context> implements
			ThreadFactory {

		/**
		 * {@inheritDoc}.This method specializes in creating a
		 * {@code ContextualThread}
		 */
		@Override
		public Thread newThread(Runnable r) {
			return new ContextualThread<Context>(r);
		}
	}

	/**
	 * Thread class with a context holding {@link ThreadLocal}
	 * 
	 * @author murthyv
	 *
	 * @param <Context>
	 */
	@Data
	@EqualsAndHashCode(callSuper = true)
	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	protected static class ContextualThread<Context> extends Thread {

		/** A ThreadLocal. */
		ThreadLocal<Context> threadLocal;// = new ThreadLocal<>();

		/** Gets the context from thread local */
		public Context getContext() {
			return threadLocal.get();
		}

		/**
		 * Constructor. if Runnable target is of contextual nature then set the
		 * context to this thread.
		 */
		public ContextualThread(final Runnable target) {
			super(target);
			final Context ctx = ContextualRunnable.getContext(target);
			threadLocal = new ThreadLocal<Context>() {
				protected Context initialValue() {
					return ctx;
				}
			};
			if (ctx != null)
				setContext(ctx);
		}

		/** Set context to ThreadLocal */
		public void setContext(Context context) {
			if (context == null)
				clearContext();
			else
				threadLocal.set(context);
			log.debug("Setting ctx:{}", threadLocal.get());

		}

		/** Clear context in ThreadLocal */
		public void clearContext() {
			threadLocal.set(null);
			threadLocal.remove();
			threadLocal.set(null);
			log.debug("Clear context:{}", threadLocal.get());
		}

		public static <Context> ContextualThread<Context> current() {
			Thread t = Thread.currentThread();
			if (t instanceof ContextualThread)
				return (ContextualThread<Context>) t;
			else
				return null;
		}
	}
}

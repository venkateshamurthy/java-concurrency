package concurrent.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A Bounding decorator for a {@link CompletionService} that bounds the limit of tasks to
 * be submitted by means of a {@link Semaphore}.
 * 
 * @author vmurthy
 * @param <V>
 *            is the result type
 */
public class BoundedCompletionService<V> implements CompletionService<V> {
	/**
	 * semaphore for bounding.
	 */
	protected Semaphore semaphore;
	/**
	 * delegateCompletionService which is the delegate
	 */
	protected CompletionService<V> delegateCompletionService;

	/**
	 * This constructor ensures only one task submission happens in a given time
	 * 
	 * @param completionService
	 *            that is to be bounded
	 */
	public BoundedCompletionService(CompletionService<V> completionService) {
		this(completionService, 1);
	}

	/**
	 * This constructor ensures a maximum of maxTasks number of submits for the
	 * {@link #delegateCompletionService}
	 * 
	 * @param completionService
	 *            to be bounded for task submission
	 * @param maxTasks
	 *            defines the bounding limit on number of tasks to be submitted
	 */
	public BoundedCompletionService(CompletionService<V> completionService,
			int maxTasks) {
		if (completionService == null)
			throw new NullPointerException(
					"Delegate Completion Service cannot be null");
		if (maxTasks < 0)
			throw new IllegalArgumentException(
					"Semaphore permits must be non-negative");
		this.delegateCompletionService = completionService;
		// Create a fair semaphore by default.
		this.semaphore = new Semaphore(maxTasks, true);
	}

	/**
	 * One semaphore permit will be releived if
	 * {@link delegateCompletionService#poll()} is successful
	 */
	public Future<V> poll() {
		Future<V> future = delegateCompletionService.poll();
		if (future != null)
			semaphore.release();
		return future;
	}

	/**
	 * One semaphore permit will be releived if
	 * {@link delegateCompletionService#poll(timeout, unit)} is successful
	 */
	public Future<V> poll(long timeout, TimeUnit unit)
			throws InterruptedException {
		Future<V> future = delegateCompletionService.poll(timeout, unit);
		if (future != null)
			semaphore.release();
		return future;
	}

	/**
	 * First a semaphore permit would be acquired before
	 * {@link delegateCompletionService#submit(task)} is called
	 * 
	 * @param task
	 *            is the {@link Callable} to be submitted
	 * @return future
	 */
	public Future<V> submit(Callable<V> task) {
		try {
			semaphore.acquire();
		} catch (InterruptedException ie) {
			throw new RejectedExecutionException(ie);
		}
		Future<V> future = null;
		try {
			future = delegateCompletionService.submit(task);
		} catch (RuntimeException re) {
			semaphore.release();
			throw re;
		}
		return future;
	}

	/**
	 * First a semaphore permit would be acquired before
	 * {@link delegateCompletionService#submit(task)} is called
	 * 
	 * @param task
	 *            is the {@link Runnable} to be submitted
	 * @param V
	 *            is the type of result
	 * @return future
	 */
	public Future<V> submit(Runnable task, V result) {
		return submit(Executors.callable(task, result));
	}

	/**
	 * One semaphore permit will be released per every take call and that would
	 * be subsequent to {@link delegateCompletionService#take()}
	 * 
	 * @return future
	 */
	public Future<V> take() throws InterruptedException {
		Future<V> future = delegateCompletionService.take();
		semaphore.release();
		return future;
	}
}

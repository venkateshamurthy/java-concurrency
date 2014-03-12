package concurrent.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
/**
 * This class is a  {@link TransactionalCompletionService} adapter to {@link BoundedCompletionService}.<br>
 * <p>
 * It basically offers a transactional behavior adapter to a Bounded Completion Service.<br>
 * All the transactional completion service "specific" methods will be dealt as is by the TransactionalCompletionService handle
 * <br>and all the task submission and Poll related methods are handed off to BoundedCompletionService handle 
 * @author vmurthy
 *
 * @param <V>
 */
public class BoundedTransactionalCompletionService<V> implements
		TransactionalCompletionService<V> {
	/**
	 * TransactionalCompletionService handle of the same service passed
	 */
	private final TransactionalCompletionService<V> transactionalCompletionService;

	/**
	 * BoundedCompletionServce handle of the same service passed
	 */
	private final CompletionService<V> boundedCompletionService;

	/**
	 * Constructor constructs a bounded and a transactional flavor of passed
	 * completion service
	 * 
	 * @param completionService
	 *            is the passed instance of {@link CompletionService} to be
	 *            decorated
	 * @param maxTasks
	 *            is the number of semaphore permits
	 */
	public BoundedTransactionalCompletionService(
			CompletionService<V> completionService, int maxTasks) {
		this(new SimpleTransactionalCompletionService<V>(completionService),
				maxTasks);
	}

	/**
	 * Constructor constructs a bounded flavor of passed in
	 * {@link TransactionalCompletionService}
	 * 
	 * @param transactionalCompletionService
	 *            is the passed in service
	 * @param maxTasks
	 *            is the number of semaphore permits
	 */
	public BoundedTransactionalCompletionService(
			TransactionalCompletionService<V> transactionalCompletionService,
			int maxTasks) {
		this.transactionalCompletionService = transactionalCompletionService;
		this.boundedCompletionService = new BoundedCompletionService<V>(
				transactionalCompletionService, maxTasks);
	}

	/**
	 * delegated to {@link TransactionalCompletionService#startTransaction()}
	 */
	public void startTransaction() {
		transactionalCompletionService.startTransaction();
	}

	/**
	 * delegated to {@link TransactionalCompletionService#endTransaction()}
	 */
	public void endTransaction() {
		transactionalCompletionService.endTransaction();
	}

	/**
	 * delegated to {@link TransactionalCompletionService#cancelTransaction()}
	 */
	public void cancelTransaction() {
		transactionalCompletionService.cancelTransaction();
	}

	/**
	 * delegated to {@link TransactionalCompletionService#isInTransaction()}
	 */
	public boolean isInTransaction() {
		return transactionalCompletionService.isInTransaction();
	}

	/**
	 * delegated to {@link
	 * TransactionalCompletionService.isTransactionFinished()}
	 */
	public boolean isTransactionFinished() {
		return transactionalCompletionService.isTransactionFinished();
	}

	/**
	 * delegated to {@link BoundedCompletionService#submit(Callable)}
	 */
	public Future<V> submit(Callable<V> task) {
		return boundedCompletionService.submit(task);
	}

	/**
	 * delegated to {@link BoundedCompletionService#submit(Runnable, Object)}
	 */
	public Future<V> submit(Runnable task, V result) {
		return boundedCompletionService.submit(task, result);
	}

	/**
	 * delegated to {@link BoundedCompletionService#take()}
	 */
	public Future<V> take() throws InterruptedException {
		return boundedCompletionService.take();
	}

	/**
	 * delegated to {@link BoundedCompletionService#poll()}
	 */
	public Future<V> poll() {
		return boundedCompletionService.poll();
	}

	/**
	 * delegated to {@link BoundedCompletionService#poll(long, TimeUnit)}
	 */
	public Future<V> poll(long timeout, TimeUnit unit)
			throws InterruptedException {
		return boundedCompletionService.poll(timeout, unit);
	}
}

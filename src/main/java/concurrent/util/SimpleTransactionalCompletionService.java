package concurrent.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;

/**
 * An implementation of {@link TransactionalCompletionService}. Most method
 * actions are delegated to a delegateCompletionService
 * 
 * @author vmurthy
 */
@Log4j2
@Data
@FieldDefaults(level=AccessLevel.PRIVATE,makeFinal=true)
public class SimpleTransactionalCompletionService<V> implements
		TransactionalCompletionService<V> {
	/**
	 * A thread visible flag to indicate whether this is in transaction
	 */
	@NonFinal volatile boolean isInTransaction;
	/**
	 * A delegate Completion Service
	 */
	CompletionService<V> delegateCompletionService;
	/**
	 * A List of future objects
	 */
	List<Future<V>> futureQueue;
	/**
	 * A {@link Lock} object used to control thread safety in accessing methods.
	 */
	Lock serviceLock;

	/**
	 * A poll duration to be used in take method,<br>
	 * TBD: (Do i need to make this configurable?)
	 * 
	 * @see #take()
	 */
	static int defaultPollDurationInMilliSeconds = 20;

	/**
	 * Constructor. It has a waitingQueue for tasks and receives a delegate
	 * {@link CompletionService}
	 * 
	 * @param completionService
	 *            The {@link CompletionService} to delegate to.
	 */
	public SimpleTransactionalCompletionService(
			CompletionService<V> completionService) {
		if (completionService == null)
			throw new NullPointerException(
					"Delegate CompletionService cannot be null");
		this.delegateCompletionService = completionService;
		// Make sure the futureQueue is synchronized
		futureQueue = Collections.synchronizedList(new ArrayList<Future<V>>());
		serviceLock = new ReentrantLock();
	}

	/**
	 * Awaits for the {@link #serviceLock} to be available before submitting.
	 * Additionally validates for if this service is in transaction
	 */
	public Future<V> submit(Callable<V> task) {
		Future<V> future = null;
		try {
			serviceLock.lockInterruptibly();
			if (!isInTransaction)
				throw new IllegalStateException(
						"The "
								+ getClass().getName()
								+ " is not in transaction. Please make sure to call startTransaction");
			if (log.isDebugEnabled())
				log.debug("Submitting " + task);
			future = delegateCompletionService.submit(task);
			futureQueue.add(future);
		} catch (InterruptedException ie) {
		} finally {
			serviceLock.unlock();
		}
		return future;
	}

	/**
	 * Awaits for the {@link #serviceLock} to be available before submitting.
	 * Additionally validates for if this service is in transaction
	 * 
	 * @param task
	 *            is a {@link Runnable} that would be made to a callable
	 * @param result
	 *            is the result type.
	 * 
	 *            <p>
	 *            The parameters task and result will be converted to a callable
	 *            by {@link Executors#callable(Runnable, Object)} <br>
	 *            and passed to the overloaded method {@link #submit(Callable)}
	 */
	public Future<V> submit(Runnable task, V result) {
		return submit(Executors.callable(task, result));
	}

	/**
	 * take method is slightly modified to make use of a poll with a duration.
	 * and hence doesnt delegate to the take method of
	 * {@link #delegateCompletionService}.
	 * 
	 * <p>
	 * The reason being we need to take the transaction finished state into
	 * consideration.
	 */
	public Future<V> take() throws InterruptedException {
		Future<V> future = null;
		while (future == null && !isTransactionFinished()) {
			if (log.isDebugEnabled())
				log.debug("Trying to take.. ");
			future = poll();
			if (future == null) {
				synchronized (this) {
					this.wait(
							defaultPollDurationInMilliSeconds);
				}
			}
		}
		return future;
	}

	/**
	 * The poll method awaits for the lock to be available before making the
	 * poll call on the {@link #delegateCompletionService}
	 */
	public Future<V> poll() {
		Future<V> future = null;
		try {
			serviceLock.lockInterruptibly();
			if (log.isDebugEnabled())
				log.debug("Trying to poll..");
			future = delegateCompletionService.poll();
			if (future != null)
				futureQueue.remove(future);
		} catch (InterruptedException ie) {
			// What should we do here?
		} finally {
			serviceLock.unlock();
		}
		return future;
	}

	/**
	 * The poll method awaits for the lock to be available before making the
	 * poll call on the {@link #delegateCompletionService}
	 */
	public Future<V> poll(long timeOut, TimeUnit unit)
			throws InterruptedException {
		Future<V> future = null;
		try {
			serviceLock.lockInterruptibly();
			future = delegateCompletionService.poll(timeOut, unit);
			if (future != null)
				futureQueue.remove(future);
		} catch (InterruptedException ie) {
			// What should we do here?
		} finally {
			serviceLock.unlock();
		}
		return future;
	}

	/**
	 * startTransaction just sets a flag
	 */
	@Override
	public void startTransaction() {
		try {
			serviceLock.lockInterruptibly();
			log.info("Starting transaction..");
			if (futureQueue.size() > 0)
				throw new IllegalStateException();
			isInTransaction = true;
		} catch (InterruptedException ie) {
			// What should we do here?
		} finally {
			serviceLock.unlock();
		}
	}

	/**
	 * endTransaction sets the flag off.
	 */
	@Override
	public void endTransaction() {
		log.info("Ending transaction..");
		isInTransaction = false;
	}

	/**
	 * Cancels the running transaction
	 */
	@Override
	public void cancelTransaction() {
		try {
			serviceLock.lockInterruptibly();
			isInTransaction = false;
			log.info("After cancelling ;future Queue length:"
					+ futureQueue.size());
			for (Future<V> future : futureQueue)
				future.cancel(true);
			while (!isTransactionFinished())
				try {
					take();
				} catch (InterruptedException interruptedException) {
				}
		} catch (InterruptedException ie) {
			// What should we do here?
		} finally {
			serviceLock.unlock();
		}

	}

	@Override
	public boolean isInTransaction() {
		return isInTransaction;
	}

	/**
	 * isTransactionFinished checks if not in transaction and if the
	 * {@link #futureQueue} is empty
	 */
	@Override
	public boolean isTransactionFinished() {
		return !isInTransaction && futureQueue.isEmpty();
	}
}

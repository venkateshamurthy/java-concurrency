package concurrent.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
@Log4j2
public class SimpleForkJoiner<Task, InterimResult, FinalResult> implements
		ForkJoiner<Task, InterimResult, FinalResult> {
	/**
	 * A Transactional Service that works multiple similar tasks
	 */
	private final TransactionalCompletionService<InterimResult> transactionalForkedTaskService;
	/**
	 * A local completion service to control request submission and results gathering 
	 */
	@SuppressWarnings({ "rawtypes" })
	private final CompletionService localCompletionService;
	/**
	 * A local threadPool
	 */
	final ThreadPoolExecutor threadPool;

	/**
	 * Constructor
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SimpleForkJoiner(
			TransactionalCompletionService<InterimResult> transactionalCompletionService) {
		if (transactionalCompletionService == null)
			throw new NullPointerException();
		transactionalForkedTaskService = transactionalCompletionService;
		threadPool = new ThreadPoolExecutor(2, 2, Long.MAX_VALUE,
				TimeUnit.MICROSECONDS, new ArrayBlockingQueue(2),
				SimpleThreadFactory.of());
		localCompletionService = new ExecutorCompletionService(threadPool);
	}
	/**
	 * Execute
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public FinalResult execute(final Task task,
			final Forker<Task, InterimResult> forker,
			final Joiner<InterimResult, FinalResult> joiner)
			throws ExecutionException {
		
		if (task == null || forker == null || joiner == null)
			throw new NullPointerException();
		
		FinalResult result = null;
		int tasksPending = 0;
		
		Future<FinalResult> finalResult = null;
		
		transactionalForkedTaskService.startTransaction();
		
		localCompletionService.submit(new Callable() {
			public Object call() throws Exception {
				transactionalForkedTaskService.startTransaction();
				forker.fork(task, transactionalForkedTaskService);
				transactionalForkedTaskService.endTransaction();
				return null;
			}
		});
		// Always clear before use
		joiner.clear();
		// Now submit the Joiner callable
		finalResult = localCompletionService
				.submit(new Callable<FinalResult>() {
					public FinalResult call() throws Exception {
						while (!transactionalForkedTaskService.isTransactionFinished()) {
							Future<InterimResult> interimResult = transactionalForkedTaskService
									.take();
							if (interimResult != null)
								joiner.join(interimResult.get());
							else
								log.warn("Null Future from forker Callable");
						}
						return joiner.getResult();
					}
				});
		tasksPending = 2; 
		// upon
		try {
			while (tasksPending > 0) {
				Future future = localCompletionService.take();
				--tasksPending;
				future.get();
			}
		} catch (Throwable throwable) {
			transactionalForkedTaskService.cancelTransaction();
			// Drain the internalCompletionService
			while (tasksPending > 0)
				try {
					localCompletionService.take();
					--tasksPending;
				} catch (InterruptedException interruptedException) {
					// We cannot bail from here. The completion service must be
					// completely drained.
				}
			throw new ExecutionException(throwable);
		}
		try {
			result = finalResult.get();
		} catch (InterruptedException interruptedException) {
			throw new ExecutionException(interruptedException);
		}
		return result;
	}

	/**
	 * shuts down any resources here
	 */
	public void shutdown() {
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cancel the splitter completion service
	 */
	public void cancel() {
		transactionalForkedTaskService.cancelTransaction();
	}
}
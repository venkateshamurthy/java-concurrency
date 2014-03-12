package concurrent.util;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This policy basically tries to block-wait for a given time and tries to
 * reinsert into the ThreadPoolExecutor's queue.
 * <p>
 * Make sure to have a fixed length queue for thread pool executor
 * 
 * @author vmurthy
 * 
 */
public class ResubmitAfterWaitPolicy implements RejectedExecutionHandler {
	private final long timeOut;
	private final TimeUnit timeUnit;

	public ResubmitAfterWaitPolicy() {
		this(Long.MAX_VALUE, TimeUnit.SECONDS);
	}

	public ResubmitAfterWaitPolicy(long timeOut, TimeUnit timeUnit) {
		super();
		this.timeOut = (timeOut < 0 ? Long.MAX_VALUE : timeOut);
		this.timeUnit = timeUnit;
	}

	/**
	 * Need to re-submit
	 */
	public void rejectedExecution(Runnable runnable,
			ThreadPoolExecutor threadPool) {
		try {
			if (threadPool.isShutdown()
					|| !threadPool.getQueue()
							.offer(runnable, timeOut, timeUnit)) {
				throw new RejectedExecutionException();
			}
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			throw new RejectedExecutionException(ie);
		}
	}
}

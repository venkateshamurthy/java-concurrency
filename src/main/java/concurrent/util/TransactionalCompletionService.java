package concurrent.util;

import java.util.concurrent.CompletionService;

/**
 * This interface extends the capability of {@link CompletionService} by adding
 * a transactional behavior.
 * 
 * <p>
 * The transactional behavior can be utilized for submitting a set of tasks
 * within a transactional boundary <br>
 * and this boundary will be defined by {@link #startTransaction()} and
 * {@link #endTransaction()}.
 * 
 * <p>
 * Explaining further, the task submission can only happen after the start of
 * transaction and before <br>
 * the end of transaction. After the end of transaction; you could drain the results out of the
 * CompletionService;<br>
 * however no new submission of tasks are permitted. <br>
 * 
 * <p>
 * This is much akin to the standard database transaction except for no commit
 * and rollback concept. <br>
 * Standard {@link ExecutorService} may reject based on number of tasks etc and on different policies ; <br>
 * however a need for application to define the submission boundary is much desired to be able to <br>
 * give this flexibility to end-user. 
 * 
 * @author vmurthy
 */
public interface TransactionalCompletionService<V> extends CompletionService<V> {
	/**
	 * Enables the {@link CompletionService} to accept submission of tasks. The implementations may simply set a flag.
	 */
	public void startTransaction();

	/**
	 * Enables the {@link CompletionService} to reject submission of tasks
	 */
	public void endTransaction();

	/**
	 * Cancels the entire transaction. It makes best effort to cancel the unstarted/unfinished tasks.
	 * It all depends on <b>interrupt-ability of tasks</b> for this to succeed.
	 */
	public void cancelTransaction();

	/**
	 * Check to see if the transaction is in progress
	 * 
	 * @return Progress state
	 */
	public boolean isInTransaction();

	/**
	 * Check to see if the transaction is finished
	 * 
	 * @return true if transaction has finished.
	 */
	public boolean isTransactionFinished();
}

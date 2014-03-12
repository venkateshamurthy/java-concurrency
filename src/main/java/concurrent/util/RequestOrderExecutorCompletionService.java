package concurrent.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * RequestOrderExecutorCompletionService; as the name implies is an {@link CompletionService} implementation <br>
 * that <b>renders the result (in the take/poll methods) of each task in the same order as the request task submission</b>.
 * 
 * @author vmurthy
 *
 * @param <V>
 */
public class RequestOrderExecutorCompletionService<V> implements CompletionService<V> {
    /**
     * executor that needs to be wrapped
     */
    private final Executor                 executor;
    /**
     * requestTaskQueue basically holds the Futures in the order of
     *              requests that are taken
     */
    private final BlockingQueue<Future<V>> requestTaskQueue;

    /**
     * Ctor
     * As expected; the Blocking Queue will need to maintain request order (such as LinkedBlockingQueue)
     * @param executor
     */
    public RequestOrderExecutorCompletionService(Executor executor) {
        if (executor == null) throw new NullPointerException();
        this.executor = executor;
        this.requestTaskQueue = new LinkedBlockingQueue<Future<V>>();
    }

    /**
     * Ctor
     * 
     * @param executor
     * @param requestQueue
     *            could be a BlockingQueue that adheres to order of insertion
     */
    public RequestOrderExecutorCompletionService(Executor executor,
            BlockingQueue<Future<V>> requestQueue) {
        if (executor == null || requestQueue == null)
            throw new NullPointerException();
        this.executor = executor;
        this.requestTaskQueue = requestQueue;
    }

    /**
     * In this method we just add the future task to {@link #requestTaskQueue}.<br>
     * This {@link #requestTaskQueue} also serves as the response queue
     */
    public Future<V> submit(Callable<V> task) {
        if (task == null) throw new NullPointerException();
        FutureTask<V> futureTask = new FutureTask<V>(task);
        requestTaskQueue.add(futureTask);
        executor.execute(futureTask);
        return futureTask;
    }

    /**
     * In this method we just add the future task to {@link #requestTaskQueue}.<br>
     * This {@link #requestTaskQueue} also serves as the response queue
     */
    public Future<V> submit(Runnable task, V result) {
        return submit(Executors.callable(task, result));
    }

    /**
     * The future will be first got
     */
    public Future<V> take() throws InterruptedException {
        Future<V> future = requestTaskQueue.take();
        try {
            future.get();
        } catch (ExecutionException e) {
        }
        return future;
    }

    /**
     * Poll will basically check if it is really ready/done (so do a peek)and then do an actual poll
     */
    public Future<V> poll() {
        Future<V> future = requestTaskQueue.peek();
        if (future != null) if (future.isDone())
            requestTaskQueue.poll();
        else
            future = null;
        return future;
    }

    /**
     * If after peeking u get a non null future; then wait on it and return
     */
    public Future<V> poll(long timeout, TimeUnit unit)
            throws InterruptedException {
        Future<V> future = requestTaskQueue.peek();
        if (future != null) try {
            future.get(timeout, unit);
            requestTaskQueue.poll();
        } catch (TimeoutException e) {
            future = null; // dont poll in this case this can be valid; u just
            // need to retry
        } catch (ExecutionException ee) {
            requestTaskQueue.poll();
        }
        return future;
    }
}

package concurrent.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ParallelTask<V> implements Future<Collection<V>> {

  // FutureTask to release Semaphore as completed
  private class SemaphoreBasedFuture extends FutureTask<V> {
    SemaphoreBasedFuture(Callable<V> c) { super(c); }
    SemaphoreBasedFuture(Runnable t, V r) { super(t, r); }
    protected void done() {
      semaphore.release();
      responseQueue.add(this);
    }
  }

  private final List<SemaphoreBasedFuture> requestQueue;
  private final BlockingQueue<SemaphoreBasedFuture> responseQueue;
  private final Semaphore semaphore;
  private final Executor executor;
  private final int size;
  private boolean cancelled = false;

  public ParallelTask(Executor service, Collection<Callable<V>> callable, int permits) {
    if (service == null || callable == null) throw new NullPointerException();
		
    executor = service;
    semaphore = new Semaphore(permits);
    size = callable.size();
    requestQueue = new ArrayList<SemaphoreBasedFuture>(size);
    responseQueue = new LinkedBlockingQueue<SemaphoreBasedFuture>(size);
    for (Callable<V> c : callable) {
      requestQueue.add(new SemaphoreBasedFuture(c));
    }
  }

  public boolean cancel(boolean mayInterruptIfRunning) {
    if (isDone()) return false;
    cancelled = true;
    for (Future<?> f : requestQueue) {
      f.cancel(mayInterruptIfRunning);
    }
    return cancelled;
  }

  public List<V> get() throws InterruptedException, ExecutionException {
    List<V> result = new ArrayList<V>(requestQueue.size());
    boolean done = false;
    try {
      for (SemaphoreBasedFuture f : requestQueue) {
        if (isCancelled()) break;
        semaphore.acquire();
        executor.execute(f);
      }
      for (int i = 0; i < size; i++) {
        if (isCancelled()) break;
        result.add(responseQueue.take().get());
      }
      done = true;
    } finally {
      if (!done) cancel(true);
    }
    return result;
  }

  public List<V> get(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException {
    long totalTime = System.nanoTime() + unit.toNanos(timeout);
    boolean done = false;
    List<V> result = new ArrayList<V>(requestQueue.size());
    try {
      for (SemaphoreBasedFuture f : requestQueue) {
        if (System.nanoTime() >= totalTime) throw new TimeoutException();
        if (isCancelled()) break;
        semaphore.acquire();
        executor.execute(f);
      }
      for (int i = 0; i < size; i++) {
        if (isCancelled()) break;
        long nowTime = System.nanoTime();
        if (nowTime >= totalTime) throw new TimeoutException();
        SemaphoreBasedFuture f = responseQueue.poll(totalTime - nowTime, TimeUnit.NANOSECONDS);
        if (f == null) throw new TimeoutException();
        result.add(f.get());
      }
      done = true;
    } finally {
      //This is the thing; u will need to cancel all tasks if u aren't done	
      if (!done) cancel(true);
    }
    return result;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public boolean isDone() {
    return responseQueue.size() == size;
  }
  
  public static void main(String [] args) throws InterruptedException, ExecutionException, TimeoutException{
	  ExecutorService service = Executors.newFixedThreadPool(10);
      Collection<Callable<Void>> taskCollection = new ArrayList<Callable<Void>>(10);
      ParallelTask<Void> pTask= new ParallelTask<Void>(service,taskCollection,5);
      List<Void> futures = pTask.get(30, TimeUnit.MINUTES);
  }
}
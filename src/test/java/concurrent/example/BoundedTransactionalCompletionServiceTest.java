package concurrent.example;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import concurrent.util.BoundedTransactionalCompletionService;
import concurrent.util.TransactionalCompletionService;


public class BoundedTransactionalCompletionServiceTest extends BaseTestCase {
    ExecutorService e = null;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(BoundedTransactionalCompletionServiceTest.class);
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        e = Executors.newCachedThreadPool();
    }

    @Override
    @After
    public void tearDown() {
        joinPool(e);
        super.tearDown();
    }

    /**
     * Creating a new ECS with null Executor throw NPE
     */
    @Test(expected = NullPointerException.class)
    public void testConstructorNPE() throws NullPointerException {
        new BoundedTransactionalCompletionService<Void>(null, 0);
        shouldThrow();
    }

    /**
     * Creating a new ECS with null queue throw NPE
     */
    @Test(expected = NullPointerException.class)
    public void testConstructorNPE2() throws NullPointerException {
        new BoundedTransactionalCompletionService<Void>(null, 1);
        shouldThrow();
    }

    /**
     * Submitting a null callable throws NPE
     */
    @Test(expected = NullPointerException.class)
    public void testSubmitNPE() throws Exception {
        TransactionalCompletionService<Void> completionService = new BoundedTransactionalCompletionService<Void>(
                new ExecutorCompletionService<Void>(e), 1);
        Callable<Void> c = null;
        completionService.startTransaction();
        completionService.submit(c);
        shouldThrow();
    }

    /**
     * Submitting a null runnable throws NPE
     */
    @Test(expected = NullPointerException.class)
    public void testSubmitNPE2() throws Exception {
        TransactionalCompletionService<Boolean> ecs = new BoundedTransactionalCompletionService<Boolean>(
                new ExecutorCompletionService<Boolean>(e), 1);
        Runnable r = null;
        ecs.submit(r, Boolean.TRUE);
        shouldThrow();
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionForSubmitBeforeStartBatch() {
        BoundedTransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        Assert.assertFalse(ecs.isInTransaction());
        Callable<String> c = new StringTask();
        ecs.submit(c);
    }

    /**
     * A taken submitted task is completed
     */
    @Test
    public void testTake() {
        TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        try {
            Callable<String> c = new StringTask();
            ecs.startTransaction();
            ecs.submit(c);
            Future<String> f = ecs.take();
            Assert.assertTrue(f.isDone());
            Assert.assertTrue(ecs.isInTransaction());
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * Take returns the same future object returned by submit
     */
    @Test
    public void testTake2() {
        TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        try {
            Callable<String> c = new StringTask();
            ecs.startTransaction();
            Future<String> f1 = ecs.submit(c);
            Future<String> f2 = ecs.take();
            Assert.assertSame(f1, f2);
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    @Test
    public void testNonBoundedTransactionalTakeAfterEndBatch() {
        TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        try {
            ecs.endTransaction(); //first call complete call
            Assert.assertNull(ecs.take());// now take wont block as cs is 
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    @Test
    public void testTakeAfterEndBatch() {
        TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        try {
            ecs.startTransaction();
            ecs.submit(new StringDelayableCallable("S", 2));
            ecs.endTransaction(); //first call complete call
            Thread.sleep(1000);//just introduce a delay before take
            Future<String> f = ecs.take();
            Assert.assertNotNull(f);// now take should still give the future and is non null
            Assert.assertTrue(f.isDone());
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testStartBatchWhileStillQueueHasSomething() {
        TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        ecs.startTransaction();
        ecs.submit(new StringDelayableCallable("S", 2));
        ecs.startTransaction();//One should not call startTransaction or reset while queue has something
    }

    @Test
    public void testNormalStartBatch() throws InterruptedException {
        TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        ecs.startTransaction();
        ecs.submit(new StringDelayableCallable("S", 1));
        ecs.take();
        ecs.startTransaction();
        Assert.assertFalse(ecs.isTransactionFinished()); //Since it is reset/startTransaction
        ecs.submit(new StringDelayableCallable("S", 1));
        ecs.take();
        ecs.endTransaction();
        Assert.assertTrue(ecs.isTransactionFinished());
    }

    @Test
    public void testNormalEndBatch() throws InterruptedException {
        TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        ecs.startTransaction();
        ecs.submit(new StringDelayableCallable("S", 1));
        Assert.assertFalse(ecs.isTransactionFinished()); //here complete itself is false and also queue has some thing
        ecs.endTransaction();
        Assert.assertFalse(ecs.isTransactionFinished());//here complete=true but queue still not empty
        ecs.take();//
        Assert.assertTrue(ecs.isTransactionFinished());//Now both complete=true and queue is empty
    }

    @Test(expected = IllegalStateException.class)
    public void testSubmitFailureAfterEndBatch() {
        TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        ecs.endTransaction();
        ecs.submit(new StringDelayableCallable("S", 1));
    }

    /**
     * If poll returns non-null, the returned task is completed
     */
    @Test
    public void testPoll1() {
        TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        try {
            ecs.startTransaction();
            Assert.assertNull(ecs.poll());
            Callable<String> c = new StringTask();
            ecs.submit(c);
            Thread.sleep(SHORT_DELAY_MS);
            for (;;) {
                Future<String> f = ecs.poll();
                if (f != null) {
                    Assert.assertTrue(f.isDone());
                    break;
                } else
                    Assert.assertFalse(ecs.isTransactionFinished());//as pending queue has some thing
            }
            ecs.submit(new StringDelayableCallable("S", 1));
            Assert.assertNull(ecs.poll());//since the poll will not get immediate result for a callable that will take a second to complete
            Assert.assertFalse(ecs.isTransactionFinished());//as pending queue has some thing
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * If timed poll returns non-null, the returned task has to be  completed
     */
    @Test
    public void testPoll2() {
        TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        try {
            Assert.assertNull(ecs.poll());
            Callable<String> c = new StringTask();
            ecs.startTransaction();
            ecs.submit(c);
            Future<String> f = ecs.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            if (f != null) Assert.assertTrue(f.isDone());
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * If timed poll returns non-null, the returned task has to be  completed
     */
    @Test
    public void testTakeWillWaitTillSubmitCompletes() {
        final TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        final int delaySecs = 1;
        try {
            ecs.startTransaction();
            Assert.assertNull(ecs.poll());
            Thread t = new Thread() {
                @Override
                public void run() {
                    ecs.submit(new StringDelayableCallable("String delayab", delaySecs));
                }
            };
            t.start();
            long t1 = System.nanoTime();
            Future<String> f = ecs.take();
            Assert.assertNotNull(f);// this is not sure as if submit doesnt happen in time; then it can be null
            Assert.assertTrue(f.isDone());
            long t2 = System.nanoTime();
            Assert.assertTrue(t2 > t1 + delaySecs * 1000000000L); // Need to check that take will wait till submit completes
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    @Test
    public void testWaitedPollTillSubmitCompletes() {
        final TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        final int delaySecs = 1;
        try {
            Assert.assertNull(ecs.poll());
            Thread t = new Thread() {
                @Override
                public void run() {
                    ecs.startTransaction();
                    ecs.submit(new StringDelayableCallable("String delayab", delaySecs));
                }
            };
            t.start();
            Future<String> f = ecs.poll(delaySecs * 1000L + 100L, TimeUnit.MILLISECONDS);
            Assert.assertNull(f);//The future is available only after delaySec; but poll time period is less; so check for null
            Thread.sleep(delaySecs * 1000);// this delay is needed so that 
        } catch (Exception ex) {
            ex.printStackTrace();
            unexpectedException();
        }
    }

    @Test
    public void testCancelBatch() {
        TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 3);
        ecs.startTransaction();
        ecs.submit(new StringDelayableCallable("S1", 1));
        ecs.submit(new StringDelayableCallable("S2", 1));
        ecs.submit(new StringDelayableCallable("S3", 1));
        ecs.cancelTransaction();
        Assert.assertTrue(ecs.isTransactionFinished());
    }

    @Test
    public void testNotEnoughWaitingForPoll() {
        final TransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), 1);
        final int delaySecs = 1;
        Assert.assertNull(ecs.poll());
        Thread t = new Thread() {
            @Override
            public void run() {
                ecs.startTransaction();
                ecs.submit(new StringDelayableCallable("String delayab", delaySecs));
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(new Uncaught<RejectedExecutionException>());
        t.start();
        Future<String> f = null;
        Assert.assertNull(ecs.poll());
        try {
            f = ecs.poll(delaySecs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        Assert.assertNull(f);//since we dont expect f to be valid as time here is very short
    }

    /**
     * testTakeWontWaitWithinSemaphoreSize will set a non zero semaphore size and send a callable that will take a second of work.
     * <br>The observation expected is every first take() at the boundary of semaphore size it will be time consuming.
     * <br>but the next set of the takes are faster. 
     */
    @Test(timeout = 100000)
    public void testTakeWontWaitWithinSemaphoreSize() {
        final int factor = 4;
        final int semaphoreSize = 5;
        final int iterations = semaphoreSize * factor;
        final BoundedTransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), semaphoreSize);
        final int delaySecs = 1;
        try {
            Assert.assertNull(ecs.poll());
            ecs.startTransaction();
            Thread t = new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < iterations; i++) {
                        ecs.submit(new StringDelayableCallable("String delayab", delaySecs));
                        System.out.println("submit(" + i + ")");
                    }
                }
            };
            t.start();
            for (int i = 0; i < factor; i++)
                for (int j = 0; j < semaphoreSize; j++) {
                    //System.out.println("recieving i=" + i + " j=" + j);
                    long t1 = System.nanoTime();
                    Future<String> f = ecs.take();
                    long t2 = System.nanoTime();
                    System.out.println("recieved i=" + i + " j=" + j);
                    Assert.assertNotNull(f);
                    Assert.assertTrue(f.isDone());
                    System.out.println("t2-t1(ns):" + (t2 - t1));
                    if (j == 0)
                        Assert.assertTrue((t2 - t1) / 1000000L > 0L); // it should be milliseconds at the boundary of semaphore size
                    else {
                        Assert.assertTrue((t2 - t1) / 100000000L == 0L);//it should not be decaseconds
                        Assert.assertTrue((t2 - t1) / 1000L > 0L); //it should be in microseconds here
                    }
                }
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RejectedExecutionException.class)
    public void testSubmitSizeExceedingSemaphorePermits() {
        final int SEMAPHORE_SIZE = 2;
        //2. create the Semaphore controlled completion service; set semaphore to 2.
        final BoundedTransactionalCompletionService<String> ecs = new BoundedTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e), SEMAPHORE_SIZE);
        //3. Make 3 callables
        final Callable<String>[] cs = new Callable[SEMAPHORE_SIZE + 1];//delebrately have callable size greater than semaphore size
        for (int i = 0; i < cs.length; i++)
            cs[i] = new StringDelayableCallable("S" + i, 1);
        //4. Run them in a thread
        final CountDownLatch latch = new CountDownLatch(1);
        ecs.startTransaction();
        Thread t = new Thread() {
            @Override
            public void run() {
                ecs.submit(cs[0]);
                ecs.submit(cs[1]);
                latch.countDown();
                //5. After 2 submits the third submit will block. since semaphore size is 2.
                ecs.submit(cs[2]);
            }
        };
        //6. Create uncaught handler instance
        Uncaught<RejectedExecutionException> uncaught = new Uncaught<RejectedExecutionException>(); //Expecting a thread exception hence the uncaught handler
        t.setUncaughtExceptionHandler(uncaught); //set the uncaught handler
        //7. start the thread
        t.start();
        try {
            latch.await(); //latch is used just so that on exact third submit countdown happens
            Thread.sleep(1000);// allow third submit to wait for a second
            t.interrupt(); // will need to interrupt the ecs as the third submit is going to block
            t.join(); //just wait! till t joins up
        } catch (InterruptedException e1) {
            e1.printStackTrace(); //we dont care this this threads interruption
        }
        //8. if uncaught registered a exception then throw it and hence expected so test passes else its a failure
        uncaught.throwIfException();
    }
}

package concurrent.example;
import java.util.concurrent.Callable;
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

import concurrent.util.SimpleTransactionalCompletionService;
import concurrent.util.TransactionalCompletionService;

public class TransactionalCompletionServiceTest extends BaseTestCase {
    ExecutorService e = null;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TransactionalCompletionServiceTest.class);
    }

    public TransactionalCompletionServiceTest() {
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        e=Executors.newCachedThreadPool();
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
        new SimpleTransactionalCompletionService<Void>(null);
        shouldThrow();
    }

    /**
     * Creating a new ECS with null queue throw NPE
     */
    @Test(expected = NullPointerException.class)
    public void testConstructorNPE2() throws NullPointerException {
        new SimpleTransactionalCompletionService<Void>(null);
        shouldThrow();
    }

    /**
     * Submitting a null callable throws NPE
     */
    @Test(expected = NullPointerException.class)
    public void testSubmitNPE() throws Exception {
        TransactionalCompletionService<Void> completionService = new SimpleTransactionalCompletionService<Void>(
                new ExecutorCompletionService<Void>(e));
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
        TransactionalCompletionService<Boolean> ecs = new SimpleTransactionalCompletionService<Boolean>(
                new ExecutorCompletionService<Boolean>(e));
        ecs.startTransaction();
        Runnable r = null;
        ecs.submit(r, Boolean.TRUE);
        shouldThrow();
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionForSubmitBeforeStartTransactional() {
        SimpleTransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
        Assert.assertFalse(ecs.isInTransaction());
        Callable<String> c = new StringTask();
        ecs.submit(c);
    }

    /**
     * A taken submitted task is completed
     */
    @Test
    public void testTake() {
        SimpleTransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
        try {
            Callable<String> c = new StringDelayableCallable("A TEST STRING",3);
            ecs.startTransaction();//In transactional completion; u have to call startTransaction before submit
            Assert.assertTrue(ecs.isInTransaction());
            ecs.submit(c);
            Future<String> f = ecs.take();
            Assert.assertTrue(f.isDone());
            Assert.assertTrue(ecs.isInTransaction());
        } catch (Exception ex) {
        	ex.printStackTrace();
            unexpectedException(ex);
        }
    }

    /**
     * Take returns the same future object returned by submit
     */
    @Test
    public void testTake2() {
        TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
        try {
            Callable<String> c = new StringTask();
            ecs.startTransaction();
            Future<String> f1 = ecs.submit(c);
            Future<String> f2 = ecs.take();
            Assert.assertSame(f1, f2);
        } catch (Exception ex) {
            unexpectedException(ex);
        }
    }

    @Test
    public void testNonBlockingTakeAfterEndTransactional() {
        TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
        try {
            ecs.endTransaction(); //first call complete call
            Assert.assertNull(ecs.take());// now take wont block as cs is 
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    @Test
    public void testTakeAfterEndTransactional() {
        TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
        try {
            ecs.startTransaction();
            ecs.submit(new StringDelayableCallable("S", 5));
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
    public void testStartTransactionalWhileStillQueueHasSomething() {
        TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
        ecs.startTransaction();
        ecs.submit(new StringDelayableCallable("S", 2));
        ecs.startTransaction();//One should not call startTransaction or reset while queue has something
    }

    @Test
    public void testNormalStartTransactional() throws InterruptedException {
        TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
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
    public void testNormalEndTransactional() throws InterruptedException {
        TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
        ecs.startTransaction();
        ecs.submit(new StringDelayableCallable("S", 1));
        Assert.assertFalse(ecs.isTransactionFinished()); //here complete itself is false and also queue has some thing
        ecs.endTransaction();
        Assert.assertFalse(ecs.isTransactionFinished());//here complete=true but queue still not empty
        ecs.take();//
        Assert.assertTrue(ecs.isTransactionFinished());//Now both complete=true and queue is empty
    }

    @Test(expected = IllegalStateException.class)
    public void testSubmitFailureAfterEndTransactional() {
        TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
        ecs.endTransaction();
        ecs.submit(new StringDelayableCallable("S", 1));
    }

    /**
     * If poll returns non-null, the returned task is completed
     */
    @Test
    public void testPoll1() {
        TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
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
        TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
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
        final TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
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
        final TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
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
    public void testCancelTransactional() {
        TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
        ecs.startTransaction();
        ecs.submit(new StringDelayableCallable("S1", 1));
        ecs.submit(new StringDelayableCallable("S2", 1));
        ecs.submit(new StringDelayableCallable("S3", 1));
        ecs.cancelTransaction();
        Assert.assertTrue(ecs.isTransactionFinished());
    }

    @Test
    public void testNotEnoughWaitingForPoll() {
        final TransactionalCompletionService<String> ecs = new SimpleTransactionalCompletionService<String>(
                new ExecutorCompletionService<String>(e));
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
}

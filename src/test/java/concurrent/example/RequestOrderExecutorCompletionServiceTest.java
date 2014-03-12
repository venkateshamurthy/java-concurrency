package concurrent.example;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import concurrent.util.RequestOrderExecutorCompletionService;

public class RequestOrderExecutorCompletionServiceTest extends BaseTestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(RequestOrderExecutorCompletionServiceTest.class);
    }

    ExecutorService e = null;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        e = Executors.newCachedThreadPool();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        joinPool(e);
    }

    /**
     * Creating a new ECS with null Executor throw NPE
     */
    @Test(expected = NullPointerException.class)
    public void testConstructorNPE() throws Exception {
        new RequestOrderExecutorCompletionService<Void>(null);
    }

    /**
     * Creating a new ECS with null queue throw NPE
     */
    @Test(expected = NullPointerException.class)
    public void testConstructorNPE2() throws Exception {
        new RequestOrderExecutorCompletionService<Void>(e, null);
    }

    /**
     * Submitting a null callable throws NPE
     */
    @Test(expected = NullPointerException.class)
    public void testSubmitNPE() throws Exception {
        CompletionService<Void> ecs = new RequestOrderExecutorCompletionService<Void>(e);
        Callable<Void> c = null;
        ecs.submit(c);
    }

    /**
     * Submitting a null runnable throws NPE
     */
    @Test(expected = NullPointerException.class)
    public void testSubmitNPE2() {
        CompletionService<Boolean> ecs = new RequestOrderExecutorCompletionService<Boolean>(e);
        Runnable r = null;
        ecs.submit(r, Boolean.TRUE);
    }

    /**
     * A taken submitted task is completed
     */
    @Test
    public void testTake() {
        CompletionService<String> ecs = new RequestOrderExecutorCompletionService<String>(e);
        try {
            Callable<String> c = new StringTask();
            ecs.submit(c);
            Future<String> f = ecs.take();
            Assert.assertTrue(f.isDone());
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    @Test(expected = RuntimeException.class)
    public void testTakeWithoutSubmit() {
        final CompletionService<String> ecs = new RequestOrderExecutorCompletionService<String>(e);
        Uncaught<RuntimeException> uncaught = new Uncaught<RuntimeException>();
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    ecs.take();
                } catch (InterruptedException e) {
                    shouldThrow("The take method had to be broken by interrupt");
                }
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(uncaught);
        t.start();
        try {
            Thread.sleep(1000);
            t.interrupt();
            t.join();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        uncaught.throwIfException();
    }

    /**
     * Take returns the same future object returned by submit
     */
    @Test
    public void testTake2() {
        CompletionService<String> ecs = new RequestOrderExecutorCompletionService<String>(e);
        try {
            Callable<String> c = new StringTask();
            Future<String> f1 = ecs.submit(c);
            Future<String> f2 = ecs.take();
            Assert.assertSame(f1, f2);
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * If poll returns non-null, the returned task is completed
     */
    @Test
    public void testPoll1() {
        CompletionService<String> ecs = new RequestOrderExecutorCompletionService<String>(e);
        try {
            Assert.assertNull(ecs.poll());
            Callable<String> c = new StringTask();
            ecs.submit(c);
            Thread.sleep(SHORT_DELAY_MS);
            for (;;) {
                Future<String> f = ecs.poll();
                if (f != null) {
                    Assert.assertTrue(f.isDone());
                    break;
                }
            }
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * If timed poll returns non-null, the returned task is completed
     */
    @Test
    public void testPoll2() {
        CompletionService<String> ecs = new RequestOrderExecutorCompletionService<String>(e);
        try {
            Assert.assertNull(ecs.poll());
            Callable<String> c = new StringTask();
            ecs.submit(c);
            Future<String> f = ecs.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            if (f != null) Assert.assertTrue(f.isDone());
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * @author U0121670
     * Just checking for submit time future to be same as polled future and after looping
     */
    @Test
    public void testPollAndSubmitTimeFuture() {
        CompletionService<String> ecs = new RequestOrderExecutorCompletionService<String>(e);
        try {
            Assert.assertNull(ecs.poll());
            Callable<String> c = new StringDelayableCallable("S ", 1);
            Future<String> submitTimeFuture = ecs.submit(c);
            Future<String> f = null;
            do {
                System.out.println("testPoll3: polling for results....");
                f = ecs.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            } while (f == null);
            Assert.assertTrue(f.isDone());
            Assert.assertSame(submitTimeFuture, f);
            System.out.println("testPollAndSubmitTimeFuture:result " + f.get());
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    @Test
    public void testInRequestOrderExecution() {
        CompletionService<String> cs = new RequestOrderExecutorCompletionService<String>(e);
        final String testStr = "123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 10; i++)
            cs.submit(new StringDelayableCallable(i + "", 10));
        System.out.println("Waiting..");
        for (int i = 1; i < 10; i++)
            try {
                sb.append(cs.take().get());
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (ExecutionException e1) {
                e1.printStackTrace();
            }
        System.out.println(sb.toString());
        Assert.assertEquals(testStr, sb.toString());
    }
}

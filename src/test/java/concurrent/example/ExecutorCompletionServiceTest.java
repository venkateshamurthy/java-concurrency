package concurrent.example;

/*
 */
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExecutorCompletionServiceTest extends BaseTestCase {
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
        joinPool(e);
        super.tearDown();
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(ExecutorCompletionServiceTest.class);
    }

    /**
     * Creating a new ECS with null Executor throw NPE
     */
    @Test(expected = NullPointerException.class)
    public void testConstructorNPE() {
        new ExecutorCompletionService<Void>(null);
        shouldThrow();
    }

    /**
     * Creating a new ECS with null queue throw NPE
     */
    @Test(expected = NullPointerException.class)
    public void testConstructorNPE2() {
        new ExecutorCompletionService<Void>(e, null);
        shouldThrow();
    }

    /**
     * Submitting a null callable throws NPE
     */
    @Test(expected = NullPointerException.class)
    public void testSubmitNPE() {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService<Void> ecs = new ExecutorCompletionService<Void>(e);
        Callable<Void> c = null;
        ecs.submit(c);
        shouldThrow();
    }

    /**
     * Submitting a null runnable throws NPE
     */
    @Test(expected = NullPointerException.class)
    public void testSubmitNPE2() {
        ExecutorCompletionService<Boolean> ecs = new ExecutorCompletionService<Boolean>(e);
        Runnable r = null;
        ecs.submit(r, Boolean.TRUE);
        shouldThrow();
    }

    /**
     * A taken submitted task is completed
     */
    public void testTake() {
        ExecutorCompletionService<String> ecs = new ExecutorCompletionService<String>(e);
        try {
            Callable<String> c = new StringTask();
            ecs.submit(c);
            Future<String> f = ecs.take();
            Assert.assertTrue(f.isDone());
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * Take returns the same future object returned by submit
     */
    public void testTake2() {
        ExecutorCompletionService<String> ecs = new ExecutorCompletionService<String>(e);
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
    public void testPoll1() {
        ExecutorCompletionService<String> ecs = new ExecutorCompletionService<String>(e);
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
    public void testPoll2() {
        ExecutorCompletionService<String> ecs = new ExecutorCompletionService<String>(e);
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

    @Test
    public void testOutOfOrderExecution() {
        CompletionService<String> cs = new ExecutorCompletionService<String>(e);
        final String testStr = "123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 10; i++)
            cs.submit(new StringDelayableCallable(i + "", 10 - i));
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
        Assert.assertEquals(testStr, sb.reverse().toString());
    }
}

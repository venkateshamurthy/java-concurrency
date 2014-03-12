package concurrent.example;

import java.lang.Thread.UncaughtExceptionHandler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public class BaseTestCase {
    public static long SHORT_DELAY_MS;
    public static long SMALL_DELAY_MS;
    public static long MEDIUM_DELAY_MS;
    public static long LONG_DELAY_MS;

    /**
     * Returns the shortest timed delay.
     */
    protected long getShortDelay() {
        return Long.getLong("tck.shortDelay", 300).longValue();
    }

    /**
     * Sets delays as multiples of SHORT_DELAY.
     */
    protected void setDelays() {
        SHORT_DELAY_MS = getShortDelay();
        SMALL_DELAY_MS = SHORT_DELAY_MS * 5;
        MEDIUM_DELAY_MS = SHORT_DELAY_MS * 10;
        LONG_DELAY_MS = SHORT_DELAY_MS * 50;
    }

    /**
     * Initializes test to indicate that no thread assertions have failed
     */
    @Before
    public void setUp() {
        setDelays();
    }

    @After
    public void tearDown() {
    }

    protected void fail(String reason) throws RuntimeException {
        throw new RuntimeException(reason);
    }

    /**
     * Wait out termination of a thread pool or fail doing so
     */
    public void joinPool(ExecutorService exec) {
        try {
            exec.shutdown();
            exec.awaitTermination(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
            Assert.assertTrue(exec.isTerminated() || exec.isShutdown());
        } catch (SecurityException ok) {
            // Allowed in case test doesn't have privs
        } catch (InterruptedException ie) {
            fail("Unexpected exception");
        }
    }

    /**
     * fail with message "should throw exception"
     */
    public void shouldThrow() {
        fail("Should throw exception");
    }

    public void shouldThrow(String message) {
        fail("Should throw exception:" + message);
    }

    public void shouldThrow(Exception e) {
        fail("Should throw exception: details->" + e.getMessage());
    }
    public void unexpectedException() {
        fail("Unexpected exception:");
    }

    /**
     * fail with message "Unexpected exception"
     */
    public void unexpectedException(Exception ex) {
        fail("Unexpected exception:"+((ex!=null)?ex.getMessage():" "));
    }

    static class StringDelayableCallable implements Callable<String> {
        private final long   seconds;
        private final String s;

        public StringDelayableCallable(String s, int seconds) {
            this.s = s;
            this.seconds = seconds;
        }

        public String call() throws Exception {
            try {
                Thread.sleep(seconds * 1100, 1000);
            } catch (InterruptedException e) {
            }
            return s;
        }
    }

    /**
     * A convenient uncaught exception handler
     * @author vmurthy
     *
     */
    static class Uncaught<T extends RuntimeException> implements UncaughtExceptionHandler {
        private T ree;

        @SuppressWarnings("unchecked")
		public void uncaughtException(Thread t, Throwable e) {
            if (e instanceof RuntimeException) ree = (T) e;
        }

        public void throwIfException() throws RuntimeException {
            if (ree != null) throw ree;
        }

        public T get() {
            return ree;
        }
    }

    static final String TEST_STRING = "a test string";

    public static class StringTask implements Callable<String> {
        public String call() {
            return TEST_STRING;
        }
    }
}

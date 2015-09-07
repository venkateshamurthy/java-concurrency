package concurrent.util.contextual;

import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.MDC;

@Slf4j
public class MDCExecutorTest {

	Map<String, String> context = Collections.singletonMap("User", "Murthy");

	@Test(expected = AssertionError.class)
	public void testRunnableForAssertionError() {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				log.info(MDC.getCopyOfContextMap().toString());
				Assert.assertFalse(context.equals(MDC.getCopyOfContextMap()));//deliberately throwing
			}
		};
		MDCRunnable mdcRunner = MDCRunnable.of(context, runner);

		ExecutorService es = Executors.newFixedThreadPool(2);
		es.submit(mdcRunner);
		try {
			TimeUnit.SECONDS.sleep(1);
			es.shutdown();
			es.awaitTermination(-1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertNull(mdcRunner.getThrowable());
	}

	@Test
	public void testRunnable() {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				log.info(MDC.getCopyOfContextMap().toString());
				Assert.assertTrue(context.equals(MDC.getCopyOfContextMap()));
			}
		};
		MDCRunnable mdcRunner = MDCRunnable.of(context, runner);

		ExecutorService es = Executors.newFixedThreadPool(2);
		es.submit(mdcRunner);
		try {
			TimeUnit.SECONDS.sleep(1);
			es.shutdown();
			es.awaitTermination(-1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertNull(mdcRunner.getThrowable());//make sure that there is no throwable set
	}

	@Test
	public void testCallableForAssertionError() {
		Callable<Map<String, String>> caller = new Callable<Map<String, String>>() {
			@Override
			public Map<String, String> call() throws Exception {
				Map<String, String> map = MDC.getCopyOfContextMap();
				log.info(map.toString());
				Assert.assertFalse(context.equals(map));
				return map;
			}
		};

		MDCCallable<Map<String, String>> mdcCaller = MDCCallable.of(context,
				caller);

		ExecutorService es = Executors.newFixedThreadPool(2);

		try {
			Future<Map<String, String>> future = es.submit(mdcCaller);
			fail();
		} catch (AssertionError e) {
			// Dont do anything..its expected
		}

		try {
			TimeUnit.SECONDS.sleep(1);
			es.shutdown();
			es.awaitTermination(-1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(mdcCaller.getThrowable());
	}

	@Test
	public void testCallable() {
		Callable<Map<String, String>> callaer = new Callable<Map<String, String>>() {
			@Override
			public Map<String, String> call() throws Exception {
				Map<String, String> map = MDC.getCopyOfContextMap();
				log.info(map.toString());
				Assert.assertTrue(context.equals(map));
				return map;
			}
		};
		MDCCallable<Map<String, String>> mdcCaller = MDCCallable.of(context,
				callaer);

		ExecutorService es = Executors.newFixedThreadPool(2);
		Future<Map<String, String>> future = es.submit(mdcCaller);
		try {
			Assert.assertEquals(context, future.get());
			TimeUnit.SECONDS.sleep(1);
			es.shutdown();
			es.awaitTermination(-1, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
			fail();
		}
		Assert.assertNull(mdcCaller.getThrowable());
	}
}

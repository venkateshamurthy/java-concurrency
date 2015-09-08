package concurrent.util.contextual;

import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.MDC;

@Slf4j
public class MDCExecutorTest {

	Map<String, String> context = Collections.singletonMap("User", "Murthy");
	Map<String, String> newContext = Collections.singletonMap("User", "Lalit");
	ExecutorService es;

	  @Rule
	  public TestWatchman watchman= new TestWatchman() {
			/**
			 * Invoked when a test method fails
			 * 
			 * @param e 
			 * @param method
			 */
			public void failed(Throwable e, FrameworkMethod method) {
				log.debug("{} : FAILED",method.getName());
			}

			/**
			 * Invoked when a test method is about to start
			 * 
			 * @param method  
			 */
			public void starting(FrameworkMethod method) {
				log.debug("{} : STARTING",method.getName());
			}


			/**
			 * Invoked when a test method finishes (whether passing or failing)
			 * 
			 * @param method  
			 */
			public void finished(FrameworkMethod method) {
				log.debug("{} : FINISHED",method.getName());
			}
	     };
 

	@Before
	public void before() {
		es = Executors.newFixedThreadPool(10);
	}

	@After
	public void after() {
		try {
			es.shutdown();
			es.awaitTermination(-1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test(expected = AssertionError.class)
	public void testRunnableForAssertionError() {
		final CyclicBarrier cb = new CyclicBarrier(2); // since only one more
														// thread apart from
														// main
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				log.info(MDC.getCopyOfContextMap().toString());
				try {
					cb.await();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Assert.assertFalse(context.equals(MDC.getCopyOfContextMap()));// deliberately
																				// throwing
			}
		};
		MDCRunnable mdcRunner = MDCRunnable.of(context, runner);
		es.submit(mdcRunner);
		try {
			cb.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(mdcRunner.getThrowable());
	}

	@Test
	public void testRunnable() {
		final CyclicBarrier cb = new CyclicBarrier(2); // since only one more
														// thread apart from
														// main
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				log.info(MDC.getCopyOfContextMap().toString());
				try {
					cb.await();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Assert.assertTrue(context.equals(MDC.getCopyOfContextMap()));
			}
		};
		MDCRunnable mdcRunner = MDCRunnable.of(context, runner);
		es.submit(mdcRunner);
		try {
			cb.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertNull(mdcRunner.getThrowable());
	}

	@Test
	public void testCallableForAssertionError() {
		// final CyclicBarrier cb = new CyclicBarrier(2); // since only one more
		// thread apart from main
		Callable<Map<String, String>> caller = new Callable<Map<String, String>>() {
			@Override
			public Map<String, String> call() throws Exception {
				Map<String, String> map = MDC.getCopyOfContextMap();
				log.info(map.toString());
				// try {cb.await();} catch (Exception e) { e.printStackTrace();}
				Assert.assertFalse(context.equals(map));
				return map;
			}
		};

		MDCCallable<Map<String, String>> mdcCaller = MDCCallable.of(context,
				caller);
		Future<Map<String, String>> future = es.submit(mdcCaller);
		try {
			future.get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// try {cb.await();} catch (Exception e) { e.printStackTrace();}
		// after();
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
		Future<Map<String, String>> future = es.submit(mdcCaller);
		try {
			Assert.assertEquals(context, future.get());
		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
			fail();
		}
		Assert.assertNull(mdcCaller.getThrowable());
	}

	@Test
	public void testThreadSpecificMDC() {
		final CyclicBarrier barrier = new CyclicBarrier(3);
		// context.put("User", "Venkat");
		Runnable runnerWithContext = new Runnable() {
			@Override
			public void run() {
				log.info("testThreadSpecificMDC.runnerWithContext:{}", MDC
						.getCopyOfContextMap().toString());
				Assert.assertTrue(context.equals(MDC.getCopyOfContextMap()));
				try {
					barrier.await();
				} catch (Exception e) {
				}
			}
		};

		Runnable runnerWithNewContext = new Runnable() {
			@Override
			public void run() {
				log.info("testThreadSpecificMDC.runnerWithNewContext:{}", MDC
						.getCopyOfContextMap().toString());
				Assert.assertTrue(newContext.equals(MDC.getCopyOfContextMap()));
				try {
					barrier.await();
				} catch (Exception e) {
				}
			}
		};

		MDCRunnable mdcRunnerContext = MDCRunnable.of(context,
				runnerWithContext);
		MDCRunnable mdcRunnerNewContext = MDCRunnable.of(newContext,
				runnerWithNewContext);

		es.submit(mdcRunnerContext);
		es.submit(mdcRunnerNewContext);
		try {
			barrier.await();
			log.info(
					"testThreadSpecificMDC.All runnables under barrier finished : {}",
					barrier.getNumberWaiting() == 0);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error:", e);
		}
		Assert.assertNull(mdcRunnerContext.getThrowable());// make sure that
															// there is no
															// throwable set
		Assert.assertNull(mdcRunnerNewContext.getThrowable());// make sure that
																// there is no
																// throwable set

	}
}

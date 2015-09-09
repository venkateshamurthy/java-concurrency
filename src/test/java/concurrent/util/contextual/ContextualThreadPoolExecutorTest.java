package concurrent.util.contextual;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import concurrent.util.contextual.ContextualThreadPoolExecutor.ContextualThread;
@Slf4j
public class ContextualThreadPoolExecutorTest {
	private static final Map<String,String> context = Collections.singletonMap("User","murthy");
	
	private static final Map<String,String>  newContext = Collections.singletonMap("User","new");

	ContextualThreadPoolExecutor<Map<String,String>> ctp;
	@Test
	public void testForError() {

		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				ContextualThread<Map<String,String>> ct;
				if((ct=ContextualThread.current()) !=null){
					log.info("From {}.run method,expected-context:{} got-context:{}",ct.getName(),context,ct.getContext());
					Assert.assertNotSame("Context "+ct.getContext(),context, ct.getContext());	
				}
			}
		};
		//ctp.submit(runnable);
		final ContextualRunnable<Map<String,String>> ctxRunnable = ContextualRunnable.make(context,runnable);
		Future<?> f = ctp.submit(ctxRunnable);
		try{f.get();}catch(Exception e){}
		log.debug("Future: cancelled{}; done{}",f.isCancelled(),f.isDone());
		Assert.assertTrue(f.isDone());
		Assert.assertNotNull(ctxRunnable.getThrowable());
	}
	
	@Test
	public void test() {
		final CyclicBarrier cb = new CyclicBarrier(2);
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				ContextualThread<Map<String,String>> ct;
				if((ct=ContextualThread.current()) !=null){
					log.info("From {}.run method,expected-context:{} got-context:{}",ct.getName(),newContext,ct.getContext());
					try{cb.await();}catch(Exception e){}
					Assert.assertEquals("Context "+ct.getContext(),newContext, ct.getContext());	
				}
			}
		};
		//ctp.submit(runnable);
		ContextualRunnable<Map<String,String>> ctxRunnable = ContextualRunnable.make(newContext,runnable);
		ctp.submit(ctxRunnable);
		try{cb.await();}catch(Exception e){}
		Assert.assertNull(ctxRunnable.getThrowable());
	}
	
	@Before
	public void before(){
		ctp = ContextualThreadPoolExecutor.newFixedThreadPool(10);
	}
	@After
	public void after(){
		try {
			ctp.shutdown();
			ctp.awaitTermination(-1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

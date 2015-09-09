package concurrent.util.contextual;

import static org.junit.Assert.fail;

import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;

import concurrent.util.contextual.ContextualThreadPoolExecutor.ContextualThread;
@Slf4j
public class ContextualCallableTest {
	
	@Test
	public void testSetThreadContext() {
		log.info("Hi");
		final String context = "User:Murthy";
		final String newCtx="User:changed-user";
		ContextualCallable<String,String> ctxCallable = ContextualCallable.make(context,new Callable<String>(){
			@Override
			public String call() throws Exception{
				return context;
			}
		});
		try {
			Assert.assertEquals(context,ctxCallable.call());
		} catch (Exception e) {
			fail();
		}
		Assert.assertEquals(context,ctxCallable.getContext());
		ContextualThread<String> ct = new ContextualThread<>(new ContextualFutureTask<String,String>(ctxCallable));
		Assert.assertEquals(context, ct.getContext());
		ctxCallable.withContext(newCtx).setThreadContext(ct);
		Assert.assertEquals(newCtx, ct.getContext());
		Assert.assertNotNull(ct.getThreadLocal());
		ctxCallable.withContext(null).setThreadContext(ct);
		Assert.assertNull(ct.getContext(),ct.getContext());
		Assert.assertNull(ct.getThreadLocal().get());
	}
	@Test
	public void testNullContext() {
		ContextualCallable<String,String> ctxCallable = ContextualCallable.make(new Callable<String>(){
			@Override
			public String call() {
				return null;
			}
		});
		Assert.assertNull(ctxCallable.getContext());
	}
	@Test
	public void testNotNullContext() {
		final String context = "User:Murthy";
		ContextualCallable<String,String> ctxCallable = ContextualCallable.make(context,new Callable<String>(){
			@Override
			public String call() {
				return context;
			}
		});
		Assert.assertNotNull(ctxCallable.getContext());
	}
}

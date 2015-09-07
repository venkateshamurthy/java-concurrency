package concurrent.util.contextual;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;

import concurrent.util.contextual.ContextualThreadPoolExecutor.ContextualThread;
@Slf4j
public class ContextualRunnableTest {
	
	@Test
	public void testSetThreadContext() {
		log.info("Hi");
		final String context = "User:Murthy";
		final String newCtx="User:changed-user";
		ContextualRunnable<String> ctxRunnable = ContextualRunnable.make(context,new Runnable(){
			@Override
			public void run() {
				log.info("This is a user context runner ");
			}
		});
		ctxRunnable.run();
		Assert.assertEquals(context,ctxRunnable.getContext());
		ContextualThread<String> ct = new ContextualThread(ctxRunnable);
		Assert.assertEquals(context, ct.getContext());
		ctxRunnable.withContext(newCtx).setThreadContext(ct);
		Assert.assertEquals(newCtx, ct.getContext());
		Assert.assertNotNull(ct.getThreadLocal());
		ctxRunnable.withContext(null).setThreadContext(ct);
		Assert.assertNull(ct.getContext(),ct.getContext());
		Assert.assertNull(ct.getThreadLocal().get());
	}
	@Test
	public void testNullContext() {
		ContextualRunnable<String> ctxRunnable = ContextualRunnable.make(new Runnable(){
			@Override
			public void run() {
				log.info("This is a null context runner");
			}
		});
		Assert.assertNull(ctxRunnable.getContext());
	}
	@Test
	public void testNotNullContext() {
		String context = "User:Murthy";
		ContextualRunnable<String> ctxRunnable = ContextualRunnable.make(context,new Runnable(){
			@Override
			public void run() {
				log.info("This is a user context runner");
			}
		});
		Assert.assertEquals(context,ctxRunnable.getContext());
	}
}

package concurrent.util.contextual;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;

import concurrent.util.contextual.ContextualThreadPoolExecutor.ContextualThread;
@Slf4j
public class ContextualThreadPoolExecutorTest {
	private static final String context = "User:murthy";
	
	private static final String newContext = "User:new";

	@Test
	public void test() {
		ContextualThreadPoolExecutor<String> ctp= ContextualThreadPoolExecutor.newFixedThreadPool(10);
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				ContextualThread<String> ct;
				if((ct=ContextualThread.current()) !=null){
					log.info("From {}.run method,expected-context:{} got-context:{}",ct.getName(),context,ct.getContext());
					Assert.assertEquals("Context "+ct.getContext(),context, ct.getContext());	
					
				}
			}
		};
		//ctp.submit(runnable);
		ContextualRunnable<String> ctxRunnable = ContextualRunnable.make(context,runnable);
		ctp.submit(ctxRunnable);
		
		try {
			TimeUnit.SECONDS.sleep(4);
			ctp.shutdown();
			ctp.awaitTermination(-1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

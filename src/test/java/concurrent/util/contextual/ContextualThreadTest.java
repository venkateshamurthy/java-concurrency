package concurrent.util.contextual;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;

import concurrent.util.contextual.ContextualThreadPoolExecutor.ContextualThread;
@Slf4j
public class ContextualThreadTest {

	private static final String context = "User:murthy";
	
	private static final String newContext = "User:new";
	
	@Test
	public void test() {
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				ContextualThread<String> ct;
				if((ct=ContextualThread.current()) !=null){
					log.info("From run method:{}",ct.getContext());
					Assert.assertEquals(context, ct.getContext());	
				}
			}
		};
		ContextualRunnable<String> ctxRunnable = ContextualRunnable.make(context,runnable);
		ContextualThread <String> ct=new ContextualThread<String>(ctxRunnable);
		Assert.assertEquals(context, ct.getContext());	
		ct.start();
		try {
			ct.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ct.setContext(newContext);
		Assert.assertEquals(newContext, ct.getContext());
	}

}

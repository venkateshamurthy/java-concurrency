package concurrent.util.memento;

import static java.util.Collections.singletonMap;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.MDC;
@Slf4j
/**
 * a simple test for {@link ContextualCallable}
 * @author murthyv
 *
 */
public class BaseTaskTest {
	public static final String DIAGNOSTIC_CONTEXT_OPERATION_ID = "operationID";
	public static final String SPS_MAIN_OPID_FORMAT = "sps-StorageMain-%1$s-%2$s";
	 /**
         * @return a generated operationID
    */
    private static Map<String,String> getNewOperationId() {
             return singletonMap(DIAGNOSTIC_CONTEXT_OPERATION_ID,UUID.randomUUID().toString());
    }
    
    private static Map<String,String> getMainThreadOpID(){
    	long curTimeInSecs = System.currentTimeMillis()/1000;
        Random rnd = new Random(curTimeInSecs);
        return singletonMap(DIAGNOSTIC_CONTEXT_OPERATION_ID,
        		String.format(SPS_MAIN_OPID_FORMAT, curTimeInSecs,
                       rnd.nextInt(1000)));
    }
    
    private String getOpId(){
    	return MDC.get(DIAGNOSTIC_CONTEXT_OPERATION_ID);
    }
	@Test
	public void testSetOpIdCall() throws InterruptedException, ExecutionException {
		log.info("Hi");
		
		final Map<String,String> context = getNewOperationId();
		Callable<String> callable=new Callable<String>(){
			@Override
			public String call() throws Exception{
				log.info("opId={} {}",getOpId(),"Printing Something");
				return "Something";
			}
		};
		ExecutorService es = Executors.newSingleThreadExecutor();
		Future<String> f = es.submit(BaseTask.wrap(callable,context));
		Assert.assertEquals("Something",f.get());
	}
	@Test
	public void testSetOpIdRun() throws InterruptedException, ExecutionException {
		log.info("Hi");
		
		final Map<String,String> context = getMainThreadOpID();
		Runnable runnable=new Runnable(){
			@Override
			public void run() {
				log.info("opId={} {}",getOpId(),"Printing Something");
				//return "Something";
			}
		};
		ExecutorService es = Executors.newSingleThreadExecutor();
		Future<?> f = es.submit(BaseTask.wrap(runnable,context));
		Assert.assertNull(f.get());
	}	
}

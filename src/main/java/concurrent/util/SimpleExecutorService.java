package concurrent.util;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
/**
 * This is a simple wrapper to execute the task in a single threaded manner
 * @author vmurthy
 *
 */
public class SimpleExecutorService extends AbstractExecutorService {

	@Override
	public void shutdown() {
		
	}

	@Override
	public List<Runnable> shutdownNow() {
		return null;
	}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return false;
	}

	@Override
	public void execute(Runnable command) {
		command.run();
		
	}



}

/**
 * 
 */
package concurrent.examples;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
//Using lombok annotation for log4j handle
//Use log4j and hence basic configurator

/**
 * @author vmurthy
 */
// Log4j Handle creator (from lombok)
@Log4j2
public class InVisibility {
	public static void main(String[] args) {
		new InVisibility().kickOff();
	}

	private void kickOff() {
		kickOff(new UnstoppableWorker());
		kickOff(new ReasonableWorker());
		kickOff(new SmartWorker());
	}

	@SneakyThrows(InterruptedException.class)
	private void kickOff(Worker w) {
		String workerType = w.getClass().getSimpleName();
		Thread t1 = new Thread(w);
		t1.start();
		TimeUnit.SECONDS.sleep(5);
		for (int i = 0; i < 3; i++) {
			t1.join(1000);
			if (t1.isAlive()) {
				log.info(String.format("The %s couldn't be stopped!",
				        workerType));
				continue;
			} else {
				log.info(String.format("The %s Stopped!", workerType));
				break;
			}
		}
		if (t1.isAlive()) {
			log.info("NO WAY!!....WELL NUKING IT NOW!!!..WOTH ABRUPT THREAD.STOP CALL");
			LockSupport.parkNanos(3000000000L);
			t1.stop();
			log.info(String.format("The %s Stopped! atlast after nuking!!",
			        workerType));
		}
	}

	private static class UnstoppableWorker implements Worker {
		private boolean start = true;
		Stopper stopper = new Stopper(this, TimeUnit.SECONDS, 5);

		@Override
		@SneakyThrows
		public void run() {
			log.info("Starting Unstoppable Worker...");
			while (start) {
				for (int i = 0; i < 100000; i++)
					;
			}
		}

		public void stop() {
			start = false;
		}
	}

	private static class ReasonableWorker implements Worker {
		private boolean start = true;
		Stopper stopper = new Stopper(this, TimeUnit.SECONDS, 5);

		@Override
		@SneakyThrows
		public void run() {
			log.info("Starting Reasonable Worker...");
			while (Thread.currentThread().isInterrupted() || start) {
				Thread.currentThread().sleep(1000);
				log.info("doing something");
			}
			// log.info("Stopping gracefully...");
		}

		public void stop() {
			start = false;
			Thread.currentThread().interrupt();
		}
	}

	private static class SmartWorker implements Worker {
		private volatile boolean start = true;
		Stopper stopper = new Stopper(this, TimeUnit.SECONDS, 5);

		@Override
		@SneakyThrows
		public void run() {
			log.info("Starting Smart  Worker...");
			while (start) {
				for (int i = 0; i < 100000; i++)
					;
			}
			// log.info("Stopping gracefully...");
		}

		public void stop() {
			start = false;
		}
	}

	static interface Worker extends Runnable {
		public void stop();
	}

	private static class Stopper extends Thread {
		TimeUnit timeUnit;
		int value;
		private Worker callback;

		public Stopper(Worker callback, TimeUnit timeUnit, int value) {
			this.timeUnit = timeUnit;
			this.value = value;
			this.callback = callback;
			start();
		}

		@Override
		@SneakyThrows
		public void run() {
			timeUnit.sleep(value);
			log.info("Need to stop");
			callback.stop();
		}
	}
}

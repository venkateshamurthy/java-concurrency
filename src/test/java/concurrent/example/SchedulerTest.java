/**
 * 
 */
package concurrent.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;

import org.junit.Test;

/**
 * @author vmurthy
 * 
 */
@Log4j2
public class SchedulerTest {
	static final ScheduledExecutorService scheduler;
	static {
		// Add the Basic Configurator for log4j
		scheduler = Executors.newScheduledThreadPool(5);
		log.info("Starting..");
		//Beeper.main(null);
	}
	@Test
	public void test() {
		Beeper.main(null);
	}
	static class Beeper {

		public static void main(String[] args) {
			beepNow();
			while (!scheduler.isTerminated())
				;
			log.info("Shutdown");
		}

		static void beepNow() {
			log.info("Hi Iam beeping");
			final Runnable beeper = new Runnable() {
				public void run() {
					log.info("beep");
				}
			};
			final ScheduledFuture<?> beeperHandle = scheduler
					.scheduleAtFixedRate(beeper, 1, 2, TimeUnit.SECONDS);

			scheduler.schedule(new Runnable() {
				public void run() {
					beeperHandle.cancel(true);
					scheduler.shutdown();
				}
			}, 10, TimeUnit.SECONDS);
		}
	}
}

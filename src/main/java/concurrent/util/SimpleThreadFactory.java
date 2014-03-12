package concurrent.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;

/**
 * This is a specific thread factory that would be used by AMD codes. Specially
 * we may want to use this in the ExecutorServices.
 * 
 * @author vmurthy
 * 
 */
@Log4j2
@Data(staticConstructor="of")
@FieldDefaults(level=AccessLevel.PRIVATE,makeFinal=true)
public class SimpleThreadFactory implements ThreadFactory {
	/**
	 * A static incrementing thread pool number
	 */
	AtomicInteger poolNumber = new AtomicInteger(1);
	/**
	 * Group of the thread
	 */
	ThreadGroup group;
	/**
	 * Thread Number
	 */
	AtomicInteger threadIndex = new AtomicInteger(1);
	/**
	 * Thread Name prefix
	 */
	@NonFinal String namePrefix;
	/**
	 * To start with daemon behavior is off
	 */
	@NonFinal boolean daemon = false;
	/**
	 * priority of the thread
	 */
	int priority = Thread.NORM_PRIORITY;
	/**
	 * A Default name prefix (say amd)
	 */
	@NonFinal String defaultNamePrefix = "amd";

	/**
	 * A Singleton DEFAULT instance
	 */
	public static final SimpleThreadFactory DEFAULT = new SimpleThreadFactory(
			"amd", 0, true);

	/**
	 * Private constructor
	 * @param baseName of the thread pool
	 * @param poolNumber that increments every time {@link #getInstance(String, boolean) is called
	 * @param isDaemon defines whether the thread created is daemon or not
	 */
	private SimpleThreadFactory(String baseName, int poolNumber,
			boolean isDaemon) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread()
				.getThreadGroup();
		namePrefix = baseName + "-" + poolNumber + "-thread-";
	}

	/**
	 * Method to create a new thread and set any specifics.
	 */
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix
				+ threadIndex.getAndIncrement(), 0);
		//Set Daemon and priority
		t.setDaemon(daemon);
		t.setPriority(priority);
		//Set Uncaught exception handler
		t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.fatal("UNCAUGHT Exception occured for "+t.getName(), e);
			}
		});
		return t;
	}

	/**
	 * @return
	 */
	public static ThreadFactory of() {
		// TODO Auto-generated method stub
		return DEFAULT;
	}
}

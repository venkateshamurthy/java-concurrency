/**
 * 
 */
package concurrent.examples;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;
//Using lombok annotation for log4j handle
//Use log4j and hence basic configurator

/**
 * @author vmurthy
 * 
 */
@Log4j2
@Data(staticConstructor = "of")
@Accessors(fluent=true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProducerConsumer {

	/**
	 * Queue to be used within producer consumer
	 */
	@NonNull
	@NonFinal 
	@Setter
	BlockingQueue<String> queue;

	/**
	 * Count Down Latches
	 */
	@NonNull
	CountDownLatch platch, clatch, stopLatch;

	/**
	 * Cyclic Barrier(this needs setting hence non final)
	 */
	@NonNull
	@NonFinal
	@Setter
	CyclicBarrier closingBarrier;

	/**
	 * Barrier doesnt need any external setting and hence making it final
	 */
	@NonNull
	Exchanger<String> exchanger;

	/**
	 * Semaphore needs to be set externally with number of permits and hence non
	 * final
	 */
	@NonNull
	Semaphore semaphore;

	/**
	 * Phaser doesnt need any external parties to be registered and hence making
	 * it final
	 */
	@NonNull
	Phaser phaser = new Phaser();

	/**
	 * Some indexers to name instances of producer and consumer
	 */
	AtomicInteger pindex = new AtomicInteger(0);
	AtomicInteger cindex = new AtomicInteger(0);
	AtomicBoolean stop = new AtomicBoolean(false);

	/**
	 * RandomNumber generator
	 */
	static Random r = new Random();

	/**
	 * start the producer consumer with start and stop latch
	 */
	@SneakyThrows()
	public void startWithLatch() {
		new Thread("Stopper") {
			@SneakyThrows
			public void run() {
				TimeUnit.SECONDS.sleep(10);
				stop.set(true);
			}
		}.start();
		for (; platch.getCount() > 0; platch.countDown()) {
			new Thread(new ProducerWithLatch(), "Producer").start();
			log.info("platch Count:" + platch.getCount());
		}
		for (; clatch.getCount() > 0; clatch.countDown()) {
			new Thread(new ConsumerWithLatch(), "Consumer").start();
			log.info("clatch Count:" + clatch.getCount());
		}

		stopLatch.await();
		log.info("DONE ! parties awiting:" + stopLatch.getCount());

	}

	/**
	 * Start the producer consumers with start latch and ending barrier
	 */
	@SneakyThrows()
	public void startWithLatchBarrier() {
		new Thread("Stopper") {
			@SneakyThrows
			public void run() {
				TimeUnit.SECONDS.sleep(10);
				stop.set(true);
			}
		}.start();
		for (; platch.getCount() > 0; platch.countDown()) {
			new Thread(new ProducerWithLatchBarrier(), "Producer").start();
			log.info("platch Count:" + platch.getCount());
		}
		for (; clatch.getCount() > 0; clatch.countDown()) {
			new Thread(new ConsumerWithLatchBarrier(), "Consumer").start();
			log.info("clatch Count:" + clatch.getCount());
		}

		closingBarrier.await();
		log.info("DONE ! parties awiting:" + closingBarrier.getNumberWaiting());

	}

	/**
	 * Start with Exchanger
	 */
	@SneakyThrows()
	public void startWithExchanger() {

		new Thread("Stopper") {
			@SneakyThrows
			public void run() {
				TimeUnit.SECONDS.sleep(10);
				stop.set(true);
			}
		}.start();
		new Thread(new ProducerUsingExchanger(), "Producer").start();
		new Thread(new ConsumerUsingExchanger(), "Consumer").start();
		log.info("Awaiting awiting for:" + closingBarrier.getNumberWaiting()
				+ " parties:" + closingBarrier.getParties());
		closingBarrier.await();
		log.info("DONE ! parties awiting:" + closingBarrier.getNumberWaiting());

	}

	/**
	 * Start the producer consumers with semaphore
	 */
	@SneakyThrows()
	public void startWithSemaphore() {
		new Thread("Stopper") {
			@SneakyThrows
			public void run() {
				TimeUnit.SECONDS.sleep(10);
				stop.set(true);
			}
		}.start();
		for (int i = 0; i < platch.getCount(); i++) {
			new Thread(new ProducerWithSemaphore(), "Producer").start();

		}
		for (int i = 0; i < clatch.getCount(); i++) {
			new Thread(new ConsumerUsingSemaphore(), "Consumer").start();
		}

		closingBarrier.await();
		log.info("DONE ! parties awiting:" + closingBarrier.getNumberWaiting());

	}

	/**
	 * Start the producer consumers with Phaser
	 */
	@SneakyThrows()
	public void startWithPhaser() {
		phaser.register();
		for (int i = 0; i < platch.getCount(); i++) {
			new Thread(new ProducerWithPhaser(), "Producer").start();
			// log.info("producer:" + i);
		}
		for (int i = 0; i < clatch.getCount(); i++) {
			new Thread(new ConsumerWithPhaser(), "Consumer").start();
			// log.info("consumer:" + i);
		}
		if ((platch.getCount() + clatch.getCount() + 1) == phaser
				.getRegisteredParties())
			log.error(
				String.format(
						"Oh! the arrived parties count %d is different than expected %d",
						(platch.getCount() + clatch.getCount() + 1),
						phaser.getRegisteredParties()));
		log.info("Main Is starting");
		phaser.arriveAndAwaitAdvance();
		TimeUnit.SECONDS.sleep(10);
		stop.set(true);
		TimeUnit.SECONDS.sleep(1);
		phaser.arriveAndAwaitAdvance();
		phaser.arriveAndDeregister();
		TimeUnit.SECONDS.sleep(05);
		log.info("DONE ! parties awiting:" + phaser.getArrivedParties());
		

	}

	@AllArgsConstructor()
	private class ProducerWithLatch implements Runnable {

		final String id = getClass().getSimpleName() + pindex.incrementAndGet();

		@Override
		@SneakyThrows(value = { InterruptedException.class, })
		public void run() {
			platch.await();
			log.info(id + "..startring");
			while (!stop.get()) {
				queue.put((String) (id + ":" + r.nextInt()));
				TimeUnit.MILLISECONDS.sleep(500);
			}
			log.info("Stopped:" + id + ": " + stop.get());
			stopLatch.countDown();
		}

	}

	@AllArgsConstructor()
	private class ConsumerWithLatch implements Runnable {
		final String id = getClass().getSimpleName() + cindex.incrementAndGet();

		@Override
		@SneakyThrows(value = { InterruptedException.class, })
		public void run() {
			clatch.await();
			log.info(id + "..startring");
			String result;
			while (!stop.get())
				if ((result = queue.poll()) != null)
					log.info(id + ":" + result);
			log.info("Stopped:" + id + ": " + stop.get());
			stopLatch.countDown();
		}

	}

	@AllArgsConstructor()
	private class ProducerWithLatchBarrier implements Runnable {
		final String id = getClass().getSimpleName() + pindex.incrementAndGet();

		@Override
		@SneakyThrows(value = { InterruptedException.class,
				BrokenBarrierException.class })
		public void run() {
			platch.await();
			log.info(id + "..startring");
			while (!stop.get()) {
				queue.put((String) (id + ":" + r.nextInt()));
				TimeUnit.MILLISECONDS.sleep(500);
			}
			log.info("Stopped:" + id + ": " + stop.get());
			closingBarrier.await();
		}

	}

	@AllArgsConstructor()
	private class ConsumerWithLatchBarrier implements Runnable {
		final String id = getClass().getSimpleName() + cindex.incrementAndGet();

		@Override
		@SneakyThrows(value = { InterruptedException.class,
				BrokenBarrierException.class })
		public void run() {
			clatch.await();
			log.info(id + "..startring");
			String result;
			while (!stop.get())
				if ((result = queue.poll()) != null)
					log.info(id + ":" + result);
			log.info("Stopped:" + id + ": " + stop.get());
			closingBarrier.await();
		}

	}

	@AllArgsConstructor()
	private class ProducerUsingExchanger implements Runnable {

		final String id = getClass().getSimpleName() + pindex.incrementAndGet();

		@Override
		@SneakyThrows(value = { InterruptedException.class,
				BrokenBarrierException.class, TimeoutException.class })
		public void run() {
			log.info(id + "..startring");
			while (!stop.get()) {
				String s = (String) (id + ":" + r.nextInt(32));
				String item = exchanger.exchange(s, 1, TimeUnit.SECONDS);
				log.info(id + " Exchanging s:" + s + " with item:" + item);
				TimeUnit.MILLISECONDS.sleep(500);
			}
			log.info("Awaiting awiting for:"
					+ closingBarrier.getNumberWaiting() + " parties:"
					+ closingBarrier.getParties());
			closingBarrier.await();
			log.info("Stopped:" + id + ": " + stop.get());
		}

	}

	@AllArgsConstructor()
	private class ConsumerUsingExchanger implements Runnable {
		final String id = getClass().getSimpleName() + cindex.incrementAndGet();

		@Override
		@SneakyThrows(value = { InterruptedException.class,
				BrokenBarrierException.class, TimeoutException.class })
		public void run() {
			log.info(id + "..startring");
			while (!stop.get()) {
				String s = (String) (id + ":" + r.nextInt(32));
				String item = exchanger.exchange(s, 1, TimeUnit.SECONDS);
				log.info(id + " Exchanging s:" + s + " with item:" + item);
				TimeUnit.MILLISECONDS.sleep(500);
			}
			log.info("Awaiting awiting for:"
					+ closingBarrier.getNumberWaiting() + " parties:"
					+ closingBarrier.getParties());
			closingBarrier.await();
			log.info("Stopped:" + id + ": " + stop.get());
		}

	}

	@AllArgsConstructor()
	private class ProducerWithSemaphore implements Runnable {
		final String id = getClass().getSimpleName() + pindex.incrementAndGet();

		@Override
		@SneakyThrows(value = { InterruptedException.class,
				BrokenBarrierException.class })
		public void run() {
			semaphore.acquire();
			log.info(id + "..startring");
			while (!stop.get()) {
				queue.put((String) (id + ":" + r.nextInt()));
				TimeUnit.MILLISECONDS.sleep(500);
			}
			log.info("Stopped:" + id + ": " + stop.get());
			closingBarrier.await();
		}

	}

	@AllArgsConstructor()
	private class ConsumerUsingSemaphore implements Runnable {
		final String id = getClass().getSimpleName() + cindex.incrementAndGet();

		@Override
		@SneakyThrows(value = { InterruptedException.class,
				BrokenBarrierException.class // ,TimeoutException.class
		})
		public void run() {
			log.info(id + "..startring");
			while (!stop.get()) {
				String item = queue.poll(10, TimeUnit.MICROSECONDS);
				if (item != null) {
					semaphore.release();
					log.info(id + " Received item:" + item);
				}
			}
			log.info("Awaiting awiting for:"
					+ closingBarrier.getNumberWaiting() + " parties:"
					+ closingBarrier.getParties());
			closingBarrier.await();
			log.info("Stopped:" + id + ": " + stop.get());
		}
	}

	@AllArgsConstructor()
	private class ProducerWithPhaser implements Runnable {
		final String id = getClass().getSimpleName() + pindex.incrementAndGet();
		{
			log.info(id + " Is starting");
			phaser.register();
		}

		@Override
		@SneakyThrows(value = { InterruptedException.class, })
		public void run() {
			phaser.arriveAndAwaitAdvance();

			while (!stop.get()) {
				queue.put((String) (id + ":" + r.nextInt()));
				TimeUnit.MILLISECONDS.sleep(500);
			}
			log.info("Stopped:" + id + ": " + stop.get());
			phaser.arriveAndAwaitAdvance();
			phaser.arriveAndDeregister();
		}

	}

	@AllArgsConstructor()
	private class ConsumerWithPhaser implements Runnable {
		final String id = getClass().getSimpleName() + cindex.incrementAndGet();
		{
			log.info(id + " Is starting");
			phaser.register();
		}

		@Override
		public void run() {
			phaser.arriveAndAwaitAdvance();

			String result;
			while (!stop.get()) {
				if ((result = queue.poll()) != null)
					log.info(id + ":" + result);
			}
			log.info("Stopped:" + id + ": " + stop.get());
			phaser.arriveAndAwaitAdvance();
			phaser.arriveAndDeregister();
		}

	}

}

/**
 * 
 */
package concurrent.example;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

import junit.framework.Assert;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

import org.junit.Before;
import org.junit.Test;

import concurrent.examples.ProducerConsumer;

/**
 * @author vmurthy
 * 
 */
// Log4j Handle creator (from lombok)
@Log4j2
@Accessors(fluent=true)
public class TestProducerConsumer {

	int producerCount = 5, consumerCount = 2, totalCount = producerCount
			+ consumerCount;
	CyclicBarrier closingBarrier;
	BlockingQueue<String> arrayQueue, linkedQueue, priorityQueue;
	CountDownLatch producerLatch, consumerLatch, stopLatch;
	Exchanger<String> exchanger;
	ProducerConsumer pc;

	@Before
	public void before() {
		
		//Queues
		arrayQueue = new ArrayBlockingQueue<String>(10);
		linkedQueue = new LinkedBlockingQueue<>(10);
		priorityQueue = new PriorityBlockingQueue<>(10);
		
		//Barrier
		closingBarrier = new CyclicBarrier(totalCount + 1, new Runnable() {
			public void run() {
				log.info("DONE Using Closing Barrier!");
			}
		});
		
		//Latches
		producerLatch = new CountDownLatch(producerCount);
		consumerLatch = new CountDownLatch(consumerCount);
		stopLatch = new CountDownLatch(totalCount);
		
		//Exchanger
		exchanger = new Exchanger<>();
		
		Semaphore semaphore = new Semaphore(producerCount+consumerCount);
		
		//ProducerConsumer instance
		pc = ProducerConsumer.of(arrayQueue, producerLatch, consumerLatch,
				stopLatch, closingBarrier, exchanger,semaphore);

	}

	@Test
	public void testWithLatchArrayBlockingQueue() {
		testWithLatchBlockingQueue(arrayQueue);
	}

	@Test
	public void testWithLatchBarrier() {
		pc.startWithLatchBarrier();
		Assert.assertEquals(0, pc.closingBarrier().getNumberWaiting());
		Assert.assertEquals(0, pc.clatch().getCount());
		Assert.assertEquals(0, pc.clatch().getCount());
		// This is because we dont use stopLatch instead using barrier
		Assert.assertEquals(totalCount, pc.stopLatch().getCount());
	}
	
	@Test
	public void testWithSemaphore() {
		pc.startWithSemaphore();
		Assert.assertEquals(0, pc.closingBarrier().getNumberWaiting());
		// This is because we dont use stopLatch instead using barrier
		Assert.assertEquals(totalCount, pc.stopLatch().getCount());
	}
	
	@Test
	public void testWithPhaser() {
		pc.startWithPhaser();
		Assert.assertEquals(0, pc.phaser().getRegisteredParties());
		Assert.assertEquals(0, pc.phaser().getArrivedParties());
		// This is because we dont use stopLatch instead using barrier
	}
	
	@Test
	public void testWithExchanger() {
		//Here i am only testing with 3 threads one main, one producer and one consumer
		//So reset this new instance to ProducerConsumer by Setting it.
		closingBarrier=new CyclicBarrier(3);
		pc.closingBarrier(closingBarrier);
		pc.startWithExchanger();
		Assert.assertEquals(0, pc.closingBarrier().getNumberWaiting());
	}

	@Test
	public void testWithLatchLinkedBlockingQueue() {
		testWithLatchBlockingQueue(linkedQueue);

	}

	@Test
	public void testWithLatchPriorityBlockingQueue() {
		testWithLatchBlockingQueue(priorityQueue);
	}

	private void testWithLatchBlockingQueue(BlockingQueue<String> queue) {

		pc.queue(queue);
		pc.startWithLatch();
		Assert.assertEquals(0, pc.clatch().getCount());
		Assert.assertEquals(0, pc.clatch().getCount());
		Assert.assertEquals(0, pc.platch().getCount());
	}

}

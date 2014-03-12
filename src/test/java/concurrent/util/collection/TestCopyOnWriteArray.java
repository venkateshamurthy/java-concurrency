/**
 * 
 */
package concurrent.util.collection;

import static org.junit.Assert.fail;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import lombok.extern.log4j.Log4j2;

import org.junit.Test;

import util.collection.Lists;

/**
 * @author vmurthy
 * 
 */
@Log4j2
public class TestCopyOnWriteArray {

	@Test(expected = ConcurrentModificationException.class)
	public void testConcurrentModificationOnArrayList() {
		List<String> a = Lists.array.create("a", "b");
		Iterator<String> i = a.iterator();
		log.info(i.next());
		a.add("c");
		i.next();
		fail("Huh!! you shouldnt be able to do iterate after adding to a list post of iterator");
	}

	@Test
	public void testRemoveOnCopyOnWriteArrayList() {
		final List<String> a = Lists.safe.create("a", "b", "c");
		for (Iterator<String> s = a.iterator(); s.hasNext();) {
			a.remove(s.next());
			a.addAll(Lists.safe.create("a", "b", "c"));
		}
		Assert.assertNotNull(a);
		Assert.assertFalse(a.isEmpty());
	}

	@Test
	public void testAbsenceOfConcurrentModificationOnCopyOnWriteArrayList()
			throws Exception {
		final List<String> a = Lists.safe.create("a", "b");
		final Iterator<String> i = a.iterator();
		final AtomicInteger ai = new AtomicInteger(0);
		i.next();
		a.add("c");
		i.next();
		class Adder implements Callable<Boolean> {
			@Override
			public Boolean call() throws Exception {
				TimeUnit.MICROSECONDS.sleep(100);
				return a.add("d" + ai.incrementAndGet());
			}
		}
		class Getter implements Callable<Void> {
			Iterator<String> j;
			{
				j = i;
			}

			@Override
			public Void call() throws Exception {
				TimeUnit.MILLISECONDS.sleep(1);
				Void x = null;
				;
				for (String s : a) {
					log.info(s);
				}
				return x;
			}
		}

		ExecutorService es = Executors.newFixedThreadPool(5);
		for (int j = 0; j < 100 * 1; j++)
			es.submit(new Adder());
		for (int j = 0; j < 10 * 1; j++)
			es.submit(new Getter());
		es.shutdown();
		es.awaitTermination(10, TimeUnit.SECONDS);
		new Getter().call();
		log.info(ai.get());
	}
}

/**
 * 
 */
package concurrent.util.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author vmurthy
 * 
 */
@Log4j2
public class TestConcurrentMapVsHashMap {

	@Test
	public void checkMapSpeeds() {
		
		for (Enum<MapTest> e : MapTest.values())
			log.info(String.format("Time took for %s:%d", e.name(),
					e.valueOf(MapTest.class, e.name()).testAdd()));
		if(MapTest.hashmap.getDuration() < MapTest.concurrentmap.getDuration())
		log.info(
				String.format(
						"Oh! HashMap puts and gets (%d) cannot take lesser time than concurrent map  (%d) ..",
						MapTest.hashmap.getDuration(),
						MapTest.concurrentmap.getDuration()));
		if( MapTest.hashmap
						.getDuration() < MapTest.hashtable.getDuration())
		log.info(
				String.format(
						"Oh! HashMap puts and gets (%d) cannot take lesser time than hashtable puts and gets (%d)..",
						MapTest.hashmap.getDuration(),
						MapTest.hashtable.getDuration()));
	}

	/**
	 * A Sinple enum for testing different maps for speed of puts and gets
	 * 
	 * @author vmurthy
	 * 
	 */
	private enum MapTest implements TestInterface{
		
		concurrentmap() {
			ConcurrentMap<String, Integer> m1 = new ConcurrentHashMap<String,Integer>(500000, 0.9f, 2);
			{
				super.m = m1;
			}

			@Override
			public long testAdd() {
				return doTest(noThreads, this);
			}

			@Override
			public Integer putIfAbsent(String s, Integer i) {
				return m1.putIfAbsent(s, i);
			}
		},
		hashmap() {
			Map<String, Integer> m = super.m;
			{
				super.m = m = Collections.synchronizedMap(m);
			}

			@Override
			public long testAdd() {
				return doTest(noThreads, this);
			}

			@Override
			@Synchronized("m")
			public Integer putIfAbsent(String s, Integer i) {
				Integer j = null;
				if ((j = m.get(s)) == null)
					j = m.put(s, i);
				return j;
			}
		},
		hashtable() {
			Map<String, Integer> m = super.m;
			{
				m = new Hashtable<String,Integer>();
				super.m = m = Collections.synchronizedMap(m);
			}

			@Override
			public long testAdd() {

				return doTest(noThreads, this);
			}

			@Override
			@Synchronized("m")
			public Integer putIfAbsent(String s, Integer i) {
				Integer j = null;
				if ((j = m.get(s)) == null)
					j = m.put(s, i);
				return j;
			}
		};

		int noThreads = 15;

		private static class WorkerThread implements Runnable {

			private TestInterface mapTest = null;
			private Map<String, Integer> map = null;

			public WorkerThread(TestInterface mapTest2) {
				this.mapTest = mapTest2;
				map = mapTest2.getM();
			}

			@Override
			public void run() {

				for (int i = 0; i < 500000; i++) {
					// Return 2 integers between 1-1000000 inclusive
					Integer newInteger1 = (int) Math
							.ceil(Math.random() * 1000000);
					Integer newInteger2 = (int) Math
							.ceil(Math.random() * 1000000);

					// 1. Attempt to retrieve a random Integer element
					Integer retrievedInteger = map.get(String
							.valueOf(newInteger1));
					// 2. Attempt to insert a random Integer element by put
					map.put(String.valueOf(newInteger2), newInteger2);
					// Immediately assert if its there
					Assert.assertEquals(newInteger2,
							map.get(String.valueOf(newInteger2)));

					// 3. Attempt to insert a random Integer element by
					// putIfAbsent
					mapTest.putIfAbsent(String.valueOf(newInteger2),
							newInteger2);
					// Immediately assert if its there
					Assert.assertEquals(newInteger2,
							map.get(String.valueOf(newInteger2)));

				}
			}

		}
		@Override
		public long doTest(int noThreads, TestInterface mapTest) {
			long first = System.currentTimeMillis();
			long last = 0;

			ExecutorService executor = Executors.newFixedThreadPool(noThreads);

			for (int j = 0; j < noThreads; j++) {

				Runnable worker = new WorkerThread(mapTest);
				executor.execute(worker);
			}

			executor.shutdown();
			while (!executor.isTerminated()) {
			}

			last = System.currentTimeMillis();

			return duration = last - first;
		}

		/**
		 * @param s
		 * @param i
		 * @return
		 */
		public abstract Integer putIfAbsent(String s, Integer i);
		public abstract long testAdd();

		@Getter
		private long duration;
		@Getter
		Map<String, Integer> m = new HashMap<String,Integer>();

	}

}

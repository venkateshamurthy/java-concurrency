/**
 * 
 */
package concurrent.examples;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

import org.junit.Assert;
//Using lombok annotation for log4j handle

//Use log4j and hence basic configurator

/**
 * @author vmurthy
 * 
 */
// Log4j Handle creator (from lombok)
@Log4j2
public class UnAtomicity {

	public static void main(String args[]) {
		UnAtomicity ua = new UnAtomicity();
		ua.kickOffTransaction(new FixUsingSynchronized());
		ua.kickOffTransaction(new FixUsingLocks());
	}

	@SneakyThrows
	public void kickOffTransaction(final BankAccount account) {
		log.info("Starting on " + account.getClass().getSimpleName());
		final AtomicBoolean start = new AtomicBoolean(true);

		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				while (start.get()) {
					int amt = ThreadLocalRandom.current().nextInt(10, 20);
					account.deposit(amt);
					// account.wi
				}

			}

		}, account.getClass().getSimpleName() + "-0");
		t1.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.error(t.getName() + " exception:" + e.getMessage());
				start.set(false);
			}
		});

		t1.start();
		Thread[] tW = new Thread[1];
		for (int i = 0; i < tW.length; i++) {
			tW[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					while (start.get()) {
						int amt = ThreadLocalRandom.current().nextInt(1, 10);
						account.withdraw(amt);
					}

				}

			}, account.getClass().getSimpleName() + "-1");
			tW[i].setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					log.error(t.getName() + " exception:" + e.getMessage());
					start.set(false);
				}
			});

			tW[i].start();
		}
		Thread[] tR = new Thread[1];
		for (int i = 0; i < tR.length; i++) {
			tR[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					while (start.get()) {
						account.getBalance();
					}

				}

			}, account.getClass().getSimpleName() + "-1");
			tR[i].setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					log.error(t.getName() + " exception:" + e.getMessage());
					start.set(false);
				}
			});

			tR[i].start();
		}
		TimeUnit.SECONDS.sleep(10);
		start.set(false);
		t1.join();
		for (int i = 0; i < tW.length; i++)
			tW[i].join();
		for (int i = 0; i < tR.length; i++)
			tR[i].join();
		log.info(account.getClass().getName() + ":"
				+ account.getCountOfOperations() + " R=" + account.getCountR()
				+ ",W=" + account.getCountW());
	}

	@NoArgsConstructor
	private static class FixUsingSynchronized implements BankAccount {
		private volatile int balance;
		@Getter
		public AtomicLong countR = new AtomicLong(0L);
		@Getter
		public AtomicLong countW = new AtomicLong(0L);

		@Override
		@SneakyThrows
		@Synchronized
		public void deposit(int x) {
			countW.incrementAndGet();
			balance += x;
			$lock.notifyAll();
		}

		@Override
		@SneakyThrows
		@Synchronized
		public void withdraw(int x) {
			countW.incrementAndGet();
			while (x > balance)
				$lock.wait();
			balance -= x;
			Assert.assertFalse(balance < 0);
		}

		public long getCountOfOperations() {
			return countR.get() + countW.get();
		}

		/*
		 * (non-Javadoc)
		 * 
		 */
		@Override
		public synchronized int getBalance() {
			countR.incrementAndGet();
			return balance;
		}

	}

	@NoArgsConstructor
	private static class FixUsingLocks implements BankAccount {
		private volatile int balance = 0;
		@NonNull
		private final ReadWriteLock rwLock = //new ReentrantLock();
											new	ReentrantReadWriteLock
																	 (false);
																	 
		private final Lock rLock = rwLock.readLock();
		private final Lock wLock = rwLock.writeLock();

		@NonNull
		private final Condition withdrawCondition = wLock.newCondition();
		
		@Getter
		@NonNull
		private final AtomicLong countR = new AtomicLong(0L);
	
		@Getter
		@NonNull
		private final AtomicLong countW = new AtomicLong(0L);

		@Override
		@SneakyThrows
		public void deposit(int x) {
			countW.incrementAndGet();
			wLock.lock();
			try {
				balance += x;
				withdrawCondition.signalAll();
			} finally {
				wLock.unlock();
			}
		}

		@Override
		@SneakyThrows
		public void withdraw(int x) {

			if (wLock.tryLock(100, TimeUnit.MICROSECONDS)) {
				countW.incrementAndGet();
				while (x > balance)
					withdrawCondition.await();
				balance -= x;
				//Assert.assertFalse(balance < 0);
				wLock.unlock();
			} else
				/*log.debug("Tried to get Lock for withdrawal but in vain")*/;
		}

		public long getCountOfOperations() {
			return countR.get() + countW.get();
		}

		/*
		 * (non-Javadoc)
		 * 
		 */
		@Override
		public int getBalance() {
			countR.incrementAndGet();
			int bal;
			rLock.lock();
			try{
			
			bal= balance;
			}finally{
			rLock.unlock();
			}
			return bal;
		}

	}

	interface BankAccount {
		/**
		 * deposit
		 * @param x
		 */
		void deposit(int x);
		
		/**
		 * @return
		 */
		int getBalance();

		/**
		 * 
		 * @param x
		 */
		void withdraw(int x);

		long getCountOfOperations();
		
		AtomicLong getCountR();
		
		AtomicLong getCountW();
	}
}

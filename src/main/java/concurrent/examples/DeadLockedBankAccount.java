package concurrent.examples;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import concurrent.examples.InVisibility.Worker;

@Log4j2
public class DeadLockedBankAccount {
  double balance;
  int id;
  final Lock lock = new ReentrantLock();

  DeadLockedBankAccount(int id, double balance) {
    this.id = id;
    this.balance = balance;
  }

  void withdraw(double amount) {
    // Wait to simulate io like database access ...
    try {
      Thread.sleep(10l);
    } catch (InterruptedException e) {
    }
    balance -= amount;
  }

  void deposit(double amount) {
    // Wait to simulate io like database access ...
    try {
      Thread.sleep(10l);
    } catch (InterruptedException e) {
    }
    balance += amount;
  }

  static void transfer(DeadLockedBankAccount from, DeadLockedBankAccount to, double amount) {
    synchronized (from) {
      from.withdraw(amount);
      synchronized (to) {
        to.deposit(amount);
      }
    }
  }

  static void transferUsingLock(DeadLockedBankAccount from, DeadLockedBankAccount to, double amount) {
    from.lock.lock();
    from.withdraw(amount);
    to.lock.lock();
    to.deposit(amount);
    to.lock.unlock();
    from.lock.unlock();
  }

  public static void main(String[] args) {
    final DeadLockedBankAccount fooAccount = new DeadLockedBankAccount(1, 100d);
    final DeadLockedBankAccount barAccount = new DeadLockedBankAccount(2, 100d);

    new Thread() {
      public void run() {
        DeadLockedBankAccount.transferUsingLock(fooAccount, barAccount, 10d);
      }
    }.start();

    new Thread() {
      public void run() {
        DeadLockedBankAccount.transferUsingLock(barAccount, fooAccount, 10d);
      }
    }.start();

  }
}
package concurrent.examples;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class LRUCache<Key, Value> implements Closeable {
  private final int size;
  private final Queue<Key> linkedQueue;
  private final Map<Key, Value> hashMap;
  private final ReentrantReadWriteLock rwLock;
  private final ReadLock rLock;
  private final WriteLock wLock;
  private final ScheduledExecutorService es;

  public LRUCache(final int size) {
    this.size = size;
    this.linkedQueue = new ConcurrentLinkedQueue<Key>();
    this.hashMap = new ConcurrentHashMap<Key, Value>(size);
    rwLock = new ReentrantReadWriteLock();
    rLock = rwLock.readLock();
    wLock = rwLock.writeLock();
    es = Executors.newSingleThreadScheduledExecutor();
    es.scheduleWithFixedDelay(() -> cleanup(), 0, 100, TimeUnit.MICROSECONDS);
  }

  public Value get(final Key key) {
    Value value = null;
    rLock.lock();
    try {
      if ((value = hashMap.get(key)) != null) {
        wLock.lock();
        try {
          linkedQueue.remove(key);
          linkedQueue.add(key);
        } finally {
          wLock.unlock();
        }
      }
    } finally {
      rLock.unlock();
    }
    return value;
  }

  public void put(final Key key, final Value value) {
    wLock.lock();
    try {
      boolean removed=linkedQueue.removeIf(k -> hashMap.containsKey(key));
      linkedQueue.add(key);
      Value oldValue = removed ? hashMap.replace(key, value) : hashMap.put(key, value);
    } finally {
      wLock.unlock();
    }
  }

  @Override
  public void close() throws IOException {
    es.shutdown();
    linkedQueue.clear();
    hashMap.clear();
  }

  private void cleanup() {
    wLock.lock();
    try {
      Key key;
      while (linkedQueue.size() >= size && (key = linkedQueue.poll()) != null) {
        hashMap.remove(key);
      }
    } finally {
      wLock.unlock();
    }
  }
}

package util.collection;


import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * A Simple BiDirectional Map
 * <P>
 * Two Concurrent Maps are used to hold key to value and vice-versa. Choosing
 * concurrent map as its cheap dropin replacement for hashmap and is built with
 * concurrent atomic operation.
 * <p>
 * This class doesnt extend java.util.AbstractMap (as in usual cases one would
 * do that) as it bases the approach on composition of using 2 concurrent maps.
 * 
 * @author vmurthy
 * 
 * @param <K>
 * @param <V>
 */
public class BiDiMap<K, V> implements BiDirectionalMap<K, V> {

	/**
	 * One map for Key to value and another for value to key
	 */
	ConcurrentMap<K, V> kToV = null;

	/**
	 * One map for vlue to key and another for value to key
	 */
	ConcurrentMap<V, K> vToK = null;

	/**
	 * Even though concurrent hashmaps ensure operations on them as atomic;
	 * however the composite operations on key-value and value-key is not
	 * secured for concurrency.
	 * <p>
	 * Hence we also take a ReadWriteLock for the reads and write composite
	 * operation
	 */
	ReadWriteLock rwLock = new ReentrantReadWriteLock();

	/**
	 * rLock meant for read purpose
	 */
	Lock rLock = null;

	/**
	 * wLock meant for write purpose
	 */
	Lock wLock = null;

	/**
	 * Constructor initializes the read and write lock
	 */
	public BiDiMap() {
		this(16);
	}

	/**
	 * Constructor with size taht also constructs the read/write locks
	 * 
	 * @param size
	 */
	public BiDiMap(int size) {
		kToV = Maps.concurrentMap(size);
		vToK = Maps.concurrentMap(size);
		rLock = rwLock.readLock();
		wLock = rwLock.writeLock();

	}

	/**
	 * put while putting key-vlue mapping; it ensures that the existing vlue-key
	 * combintion is removed.
	 * 
	 * <pre>
	 * public V put(K k, V v) {
	 * 	wLock.lock();
	 * 	try {
	 * 		V oldV = kToV.put(k, v);
	 * 		if (oldV != null)
	 * 			vToK.remove(oldV);// remove the old value
	 * 		vToK.put(v, k);
	 * 		return oldV;
	 * 	} finally {
	 * 		wLock.unlock();
	 * 	}
	 * }
	 * </pre>
	 * 
	 */
	@Override
	public V put(K k, V v) {
		wLock.lock();
		try {
			V oldV = kToV.put(k, v);
			if (oldV != null)
				vToK.remove(oldV);// remove the old value
			vToK.put(v, k);
			return oldV;
		} finally {
			wLock.unlock();
		}
	}

	/**
	 * containsKey follows stndard approach nd delegates to kToV
	 * 
	 * <pre>
	 * public boolean containsKey(Object k) {
	 * 	rLock.lock();
	 * 	try {
	 * 		return kToV.containsKey(k);
	 * 	} finally {
	 * 		rLock.unlock();
	 * 	}
	 * }
	 * </pre>
	 */
	@Override
	public boolean containsKey(Object k) {
		rLock.lock();
		try {
			return kToV.containsKey(k);
		} finally {
			rLock.unlock();
		}
	}

	/**
	 * containsValue will take advantage of Value-Key Mapping as its hashed out
	 * so containsValue will be faster.
	 * 
	 * <pre>
	 * public boolean containsValue(Object value) {
	 * 	rLock.lock();
	 * 	try {
	 * 		// Use hashed reverse-lookup for fast access
	 * 		return vToK.containsKey(value);
	 * 	} finally {
	 * 		rLock.unlock();
	 * 	}
	 * }
	 * 
	 * </pre>
	 */
	@Override
	public boolean containsValue(Object value) {
		rLock.lock();
		try {
			// Use hashed reverse-lookup for fast access
			return vToK.containsKey(value);
		} finally {
			rLock.unlock();
		}
	}

	/**
	 * remove would remove key from kToV and as well as from vToK.
	 * 
	 * <pre>
	 * public V remove(Object k) {
	 * 	wLock.lock();
	 * 	try {
	 * 		V oldVal = kToV.remove(k);
	 * 		vToK.remove(oldVal);
	 * 		return oldVal;
	 * 	} finally {
	 * 		wLock.unlock();
	 * 	}
	 * }
	 * 
	 * </pre>
	 */
	@Override
	public V remove(Object k) {
		wLock.lock();
		try {
			V oldVal = kToV.remove(k);
			vToK.remove(oldVal);
			return oldVal;
		} finally {
			wLock.unlock();
		}
	}

	/**
	 * RemoveValue is a new method added and is not in Map interface
	 * 
	 * @param v
	 * @return Key for this value (v) removed
	 * 
	 *         <pre>
	 * public K removeValue(Object v) {
	 * 	wLock.lock();
	 * 	try {
	 * 		K oldKey = vToK.remove(v);
	 * 		kToV.remove(oldKey);
	 * 		return oldKey;
	 * 	} finally {
	 * 		wLock.unlock();
	 * 	}
	 * }
	 * 
	 * </pre>
	 */
	@Override
	public K removeValue(Object v) {
		wLock.lock();
		try {
			K oldKey = vToK.remove(v);
			kToV.remove(oldKey);
			return oldKey;
		} finally {
			wLock.unlock();
		}
	}

	/**
	 * get is delegated to kToV
	 */
	@Override
	public V get(Object k) {
		rLock.lock();
		try {
			return kToV.get(k);
		} finally {
			rLock.unlock();
		}
	}

	/**
	 * getKey is a new method added for BiDiMap and returns key for the value
	 * 
	 * @param v
	 * @returnKey for the value
	 * 
	 *            <pre>
	 * public K getKey(Object v) {
	 * 	rLock.lock();
	 * 	try {
	 * 		return vToK.get(v);
	 * 	} finally {
	 * 		rLock.unlock();
	 * 	}
	 * }
	 * </pre>
	 */
	@Override
	public K getKey(Object v) {
		rLock.lock();
		try {
			return vToK.get(v);
		} finally {
			rLock.unlock();
		}
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		rLock.lock();
		try {
			return kToV.entrySet();
		} finally {
			rLock.unlock();
		}
	}

	@Override
	public Set<K> keySet() {
		rLock.lock();
		try {
			return kToV.keySet();
		} finally {
			rLock.unlock();
		}
	}

	/**
	 * valueSet is a new method required to get the value set
	 * 
	 * @return value set
	 */
	@Override
	public Set<V> valueSet() {
		rLock.lock();
		try {
			return vToK.keySet();
		} finally {
			rLock.unlock();
		}
	}

	/**
	 * size() method checks for consistency between the ktov and vtok maps
	 */
	@Override
	public int size() {
		rLock.lock();
		try {
			assert kToV.size() == vToK.size();
					//"The Key to Value and Value to Key Map sizes must be same";
			return kToV.size();
		} finally {
			rLock.unlock();
		}
	}

	/**
	 * isEmpty() method checks for consistency between the ktov and vtok maps
	 **/
	@Override
	public boolean isEmpty() {
		rLock.lock();
		try {
			assert kToV.isEmpty() == vToK.isEmpty();
					//"The Key to Value and Value to Key Map sizes must be same");
			return kToV.isEmpty();
		} finally {
			rLock.unlock();
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		wLock.lock();
		try {
			for (Entry<? extends K, ? extends V> e : m.entrySet())
				put(e.getKey(), e.getValue());
		} finally {
			wLock.unlock();
		}
	}

	/**
	 * getValuesFor is a new method for set of keys
	 * 
	 * @param keys
	 * @return
	 */
	@Override
	public Set<V> getValuesFor(Collection<K> keys) {
		rLock.lock();
		try {
			Set<V> valueSet = Sets.linked.create();
			for (K k : keys)
				valueSet.add(kToV.get(k));
			return valueSet;
		} finally {
			rLock.unlock();
		}
	}

	/**
	 * getKeysFor is a new method for set of values
	 * 
	 * @param values
	 * @return
	 */
	@Override
	public Set<K> getKeysFor(Collection<V> values) {
		rLock.lock();
		try {
			Set<K> keySet = Sets.linked.create();
			for (V v : values)
				keySet.add(vToK.get(v));
			return keySet;
		} finally {
			rLock.unlock();
		}
	}

	@Override
	public void clear() {
		wLock.lock();
		try {
			kToV.clear();
			vToK.clear();
		} finally {
			wLock.unlock();
		}
	}

	@Override
	public Collection<V> values() {
		rLock.lock();
		try {
			return vToK.keySet();
		} finally {
			rLock.unlock();
		}
	}

	public void replace(Map<K, V> map) {
		wLock.lock();
		try {
			if (this != map){
				clear();
				putAll(map);
			}
		} finally {
			wLock.unlock();
		}
	}
}
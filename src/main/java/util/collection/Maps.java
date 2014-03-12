package util.collection;


import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.Data;
public enum Maps {
	hash {
		@Override
		public <K, V> Map<K, V> create() {
			return new HashMap<>();
		}

		@Override
		public <K, V> Map<K, V> create(int size) {
			return new HashMap<>(size);
		}
		

	},
	linked {
		@Override
		public <K, V> Map<K, V> create() {
			return new LinkedHashMap<>();
		}

		@Override
		public <K, V> Map<K, V> create(int size) {
			return new LinkedHashMap<>(size);
		}

	},
	concurrent {
		@Override
		public <K, V> Map<K, V> create() {
			return new ConcurrentHashMap<>();
		}

		@Override
		public <K, V> Map<K, V> create(int size) {
			return new ConcurrentHashMap<>(size);
		}

	},
	BIDIMAP {
		@Override
		public <K, V> Map<K, V> create() {
			return new BiDiMap<>();
		}

		@Override
		public <K, V> Map<K, V> create(int size) {
			return new BiDiMap<>(size);
		}

	};
	/**
	 * A Static Method for special map like {{@link BiDirectionalMap}
	 * 
	 * @param <K>
	 *            key of type K
	 * @param <V>
	 *            value of type V
	 * @param size
	 * @return an {@link BiDirectionalMap} instance of a specific size
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> BiDirectionalMap<K, V> bidiMap(int size) {
		return (BiDirectionalMap<K, V>) BIDIMAP.create(size);
	}

	/**
	 * A Static Method for special map like {{@link BiDirectionalMap}
	 * 
	 * @param <K>
	 *            key of type K
	 * @param <V>
	 *            value of type V
	 * @param size
	 * @return an {@link BiDirectionalMap} instance of a default size
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> BiDirectionalMap<K, V> bidiMap() {
		return (BiDirectionalMap<K, V>) BIDIMAP.create();
	}

	/**
	 * A Static Method for special map like {{@link ConcurrentMap}
	 * 
	 * @param <K>
	 *            key of type K
	 * @param <V>
	 *            value of type V
	 * @param size
	 * @return an {@link ConcurrentMap} instance of default size and is usually
	 *         {@link ConcurrentHashMap}
	 */

	@SuppressWarnings("unchecked")
	public static <K, V> ConcurrentMap<K, V> concurrentMap() {
		return (ConcurrentMap<K, V>) concurrent.create();
	}

	/**
	 * A Static Method for special map like {{@link ConcurrentMap}
	 * 
	 * @param <K>
	 *            key of type K
	 * @param <V>
	 *            value of type V
	 * @param size
	 * @return an {@link ConcurrentMap} instance of specific size and is usually
	 *         {@link ConcurrentHashMap}
	 */

	@SuppressWarnings("unchecked")
	public static <K, V> ConcurrentMap<K, V> concurrentMap(int size) {
		return (ConcurrentMap<K, V>) concurrent.create(size);
	}

	/**
	 * create method creates an instance default size nd other setting. The
	 * individual implemenations of enums may employ standard implements for the
	 * different types.
	 * 
	 * @return
	 */
	public abstract <K, V> Map<K, V> create();
	/**
	 * A copier constructor
	 * @param <K>
	 * @param <V>
	 * @param originalMap
	 * @return
	 */
	public  <K, V> Map<K, V> create(Map<K,V> originalMap){
		Map<K,V> copyMap=create();
		copyMap.putAll(originalMap);
		return copyMap;
	}
	
	public static <K,V> Map<K,V> removeKeys(Map<K,V> map,K...keys){
		for(K key:keys)
			map.remove(key);
		return map;
	}

	/**
	/**
	 * create method creates an instance default size nd other setting. The
	 * individual implemenations of enums may employ standard implements for the
	 * different types.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param size
	 * @return
	 */
	public abstract <K, V> Map<K, V> create(int size);

	/**
	 * an array of maps
	 * 
	 * @param size
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V>[] createArray(int size) {
		return (Map<K, V>[]) Array.newInstance(Map.class, size);
	}

	/**
	 * An empty Map
	 * 
	 * @return
	 */
	public static <K, V> Map<K, V> empty() {
		return Collections.<K, V> emptyMap();
	}

}
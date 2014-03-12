package util.collection;


import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A Simple BiDirectional Map Interface extending from Map
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
public interface BiDirectionalMap<K, V> extends Map<K, V> {

	/**
	 * Returns Key Set for the eligible collection values
	 * 
	 * @param values
	 * @return KeySet
	 */
	public abstract Set<K> getKeysFor(Collection<V> values);

	/**
	 * Return Value Set for Collection of Keys
	 * 
	 * @param keys
	 * @return
	 */
	public abstract Set<V> getValuesFor(Collection<K> keys);

	/**
	 * Return value as a set just like key set
	 * 
	 * @return
	 */
	public abstract Set<V> valueSet();

	/**
	 * Get Key for a value
	 * 
	 * @param v
	 * @return
	 */
	public abstract K getKey(Object v);

	/**
	 * Removes a perticular value from the map
	 * 
	 * @param v
	 * @return the Key that held the removed value.
	 */
	public abstract K removeValue(Object v);

	/**
	 * A replacement of all existing entries with new passed in map
	 * 
	 * @param map
	 *            contains the new entries
	 */
	public abstract void replace(Map<K, V> map);

}

package concurrent.util.memento;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;

/** A memento realized using map */
class MapMemento<K, V> implements ContextualMemento<Map<K, V>> {

	/** A reference to Context map from MDC */
	private final Map<K, V> context;

	/** A constructor */
	private MapMemento(Map<K, V> c) {
		Objects.requireNonNull(c);
		context = c;
	}

	@Override
	public Map<K, V> getContext() {
		return context;
	}

	/** A creation method that provide a copy of MDC context map */
	public static <K, V> ContextualMemento<Map<K, V>> create(Map<K, V> map) {
		return new MapMemento<K, V>(map == null ? Collections.<K, V> emptyMap()
				: ImmutableMap.copyOf(map));
	}
}
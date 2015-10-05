package concurrent.util.memento;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.MDC;

public interface MapContextCollection<K,V> {
	Map<K,V> getContextMap();
	void setContextMap(Map<K,V> contextMap);
	void clear();
	void clear(K key);
}
/**
 * Implement later
 * <pre>
class Log4JMDC implements MapContextCollection<String,Object>{

	@Override
	public Map<String, Object> getContextMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContextMap(Map<String, Object> contextMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear(String key) {
		// TODO Auto-generated method stub
		
	}
	
}
</pre>
*/
class Log4J2MDC implements MapContextCollection<String, String>{
	
	@Override
	public Map<String, String> getContextMap() {
		return ThreadContext.getImmutableContext();
	}

	@Override
	public void setContextMap(Map<String, String> contextMap) {
		for(Entry<String,String> e: contextMap.entrySet())
			ThreadContext.put(e.getKey(),e.getValue());
	}

	@Override
	public void clear() {
		ThreadContext.clearMap();
	}

	@Override
	public void clear(String key) {
		ThreadContext.getContext().remove(key);
	}
	
}
class Slf4JMDC implements MapContextCollection<String,String>{

	@Override
	public Map<String, String> getContextMap() {
		return MDC.getCopyOfContextMap();
	}
	@Override
	public void setContextMap(Map<String, String> contextMap) {
		MDC.setContextMap(contextMap);
	}

	@Override
	public void clear() {
		MDC.clear();
	}

	@Override
	public void clear(String key) {
		MDC.remove(key);
	}
}

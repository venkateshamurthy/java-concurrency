package concurrent.util.memento;

/**
 * A Contextual memento originator that deals with contexts managed with in a memento. 
 * Here the memento itself is modelled on {@link ContextualMemento}. The memento container / collection is assumed within.
 * <br>
 * If this interface is used within thread context; then for eg: a SLF4J MDC can be thought of as memento collector.
 * @author murthyv
 *
 * @param <Context>
 */
public interface ContextualMementoOriginator<Context> {
	/** 
	 * Save a given context to a memento
	 * 
	 * @param context to be stored
	 * @return TaskContext a memento wrapper of the actual context
	 */
	public ContextualMemento<Context> saveToMemento(Context context);
	/**
	 * Restore the state of this interface with the given memento
	 * @param memento to be restored
	 */
	public void restoreFromMemento(ContextualMemento<Context> memento);
}



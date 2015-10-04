package concurrent.util.memento;
/**
 * A memento.
 * @author murthyv
 *
 * @param <Context>
 */
public interface ContextualMemento<Context> {
	/** Return the context secured */
	Context getContext();
}


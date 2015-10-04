package concurrent.util.memento;

public interface ContextualMemento<Context> {
	/** Return the context secured */
	Context getContext();
}


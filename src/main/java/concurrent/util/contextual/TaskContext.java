package concurrent.util.contextual;

import util.NONE_CONTEXT;

public interface   TaskContext<Context> {
	NONE_CONTEXT NONE = new NONE_CONTEXT();
	Context getContext();
}


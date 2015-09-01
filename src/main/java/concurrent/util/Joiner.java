package concurrent.util;

import java.util.concurrent.ExecutionException;

public interface Joiner<InterimResult, FinalResult> {
	void clear();

	FinalResult getResult() throws ExecutionException;

	void join(InterimResult callableResult) throws ExecutionException;
}

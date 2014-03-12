package concurrent.util;

import java.util.concurrent.ExecutionException;

public interface Joiner<InterimResult, FinalResult> {
	public void clear();

	public FinalResult getResult() throws ExecutionException;

	public void join(InterimResult callableResult) throws ExecutionException;
}

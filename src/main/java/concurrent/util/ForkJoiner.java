package concurrent.util;

import java.util.concurrent.ExecutionException;
/**
 * A Simple fork joiner interface
 * @author vmurthy
 *
 * @param <Task>
 * @param <InterimResult>
 * @param <FinalResult>
 */
public interface ForkJoiner<Task, InterimResult, FinalResult> {
	public void cancel();

	public FinalResult execute(Task request,
			Forker<Task, InterimResult> forker,
			Joiner<InterimResult, FinalResult> joiner)
			throws ExecutionException;

	public void shutdown();
}
